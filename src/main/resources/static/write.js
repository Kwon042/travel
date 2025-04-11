let selectedFiles = [];
let selectedMainIndex = null;

document.querySelector("form").addEventListener("submit", function(event) {
    event.preventDefault();

    const submitButton = document.querySelector("button[type='submit']");
    submitButton.disabled = true;

    const form = event.target;
    const formData = new FormData(form);
    const csrfToken = document.querySelector("input[name='_csrf']").value;

    // Toast UI Editor의 내용을 가져와서 폼 데이터에 추가
    const content = editor.getMarkdown(); // 에디터의 마크다운 내용 가져오기
    formData.append("content", content); // 폼 데이터에 내용 추가

    formData.append("mainImageIndex", selectedMainIndex ?? 0);

    fetch(form.action, {
        method: "POST",
        body: formData,
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    })
    .then(response => {
        if (response.status === 401) {
            alert("You need to login.");
            window.location.href = "/user/login";
            return;
        }
        if (!response.ok) {
            throw new Error("Failed to save the post.");
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            const reviewBoardId = data.boardId;
            const region = data.region;
            const redirectUrl = `/board/detail/${reviewBoardId}`;

            if (selectedFiles.length > 0) {
                let imageFormData = new FormData();
                selectedFiles.forEach(file => {
                    imageFormData.append("files[]", file);
                });
                imageFormData.append("reviewBoardId", reviewBoardId);

                return fetch("/api/image/board/upload", {
                    method: "POST",
                    body: imageFormData
                })
                .then(imageResponse => {
                    if (!imageResponse.ok) {
                        throw new Error("이미지 업로드 실패");
                    }
                    return imageResponse.json();
                })
                .then(imageData => {
                    console.log("이미지 업로드 성공:", imageData.message);
                    window.location.href = redirectUrl;
                });
            } else {
                window.location.href = redirectUrl;
            }
        } else {
            alert("게시글 저장 실패");
        }
    })
    .catch(error => {
        console.error(error);
        submitButton.disabled = false;
    });
});

// 이미지 미리보기 및 삭제 처리
function previewImages(event) {
    let previewContainer = document.getElementById('imagePreview');
    previewContainer.innerHTML = '';
    selectedFiles = Array.from(event.target.files); // 최신 파일 리스트로 갱신

    if (selectedFiles.length > 0) {
        selectedFiles.forEach((file, index) => {
            let reader = new FileReader();
            reader.onload = function(e) {
                let wrapper = document.createElement("div");
                wrapper.style.position = "relative";
                wrapper.style.display = "inline-block";

                let imgElement = document.createElement("img");
                imgElement.src = e.target.result;
                imgElement.classList.add("img-thumbnail", "m-2");
                imgElement.style.width = "150px";
                imgElement.style.height = "150px";
                imgElement.dataset.index = index;

                // ❌ 삭제 버튼
                let deleteBtn = document.createElement("span");
                deleteBtn.innerHTML = "&times;";
                deleteBtn.style.position = "absolute";
                deleteBtn.style.top = "5px";
                deleteBtn.style.right = "10px";
                deleteBtn.style.cursor = "pointer";
                deleteBtn.style.color = "red";
                deleteBtn.style.fontSize = "20px";
                deleteBtn.style.fontWeight = "bold";

                deleteBtn.addEventListener("click", function() {
                    selectedFiles.splice(index, 1); // 해당 파일 제거
                    document.getElementById("image").value = ""; // 파일 인풋 초기화
                    updatePreview(); // 미리보기 갱신
                });

                // 메인 이미지 선택
                imgElement.addEventListener("click", function () {
                    document.querySelectorAll('#imagePreview img').forEach(img => {
                        img.classList.remove('main-selected');
                        img.style.border = '';
                    });
                    this.classList.add('main-selected');
                    this.style.border = '3px solid #007bff';
                    selectedMainIndex = index;
                });

                wrapper.appendChild(imgElement);
                wrapper.appendChild(deleteBtn);
                previewContainer.appendChild(wrapper);
            };
            reader.readAsDataURL(file);
        });
    }
}

function updatePreview() {
    const event = {
        target: {
            files: selectedFiles
        }
    };
    previewImages(event);
}