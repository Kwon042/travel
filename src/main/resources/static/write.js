document.querySelector("form").addEventListener("submit", function(event) {
    event.preventDefault(); // 기본 폼 제출 방지

    let submitButton = document.querySelector("button[type='submit']");
    submitButton.disabled = true; // 중복 제출 방지

    let form = event.target;
    let formData = new FormData(form);
    let csrfToken = document.querySelector("input[name='_csrf']").value;

    formData.append("mainImageIndex", selectedMainIndex ?? 0);

    // 게시글 저장
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
            const reviewBoardId = data.boardId
            const region = data.region; // 인코딩된 지역명 (서버에서 처리함)
            const redirectUrl = `/board/reviewBoard?region=${region}`;
            const files = document.getElementById("image").files;

            if (files.length > 0) { // 이미지 파일이 선택된 경우
                let imageFormData = new FormData();
                Array.from(files).forEach(file => {
                    imageFormData.append("files[]", file);
                });
                imageFormData.append("reviewBoardId", reviewBoardId); // 게시글 ID 추가

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
        submitButton.disabled = false; // 에러 발생 시 버튼 다시 활성화
    });
});

let selectedMainIndex = null;

// 이미지 미리보기를 위한 함수
function previewImages(event) {
    let previewContainer = document.getElementById('imagePreview');
    previewContainer.innerHTML = ''; // 기존 미리보기 초기화

    let files = event.target.files;
    if (files.length > 0) {
        Array.from(files).forEach(file => {
            let reader = new FileReader();
            reader.onload = function(e) {
                let imgElement = document.createElement("img");
                imgElement.src = e.target.result;
                imgElement.classList.add("img-thumbnail", "m-2");
                imgElement.style.width = "150px";
                imgElement.style.height = "150px";
                imgElement.dataset.index = index;

                // 클릭 시 메인 이미지 선택
                imgElement.addEventListener("click", function () {
                    document.querySelectorAll('#imagePreview img').forEach(img => {
                        img.classList.remove('main-selected');
                        img.style.border = '';
                    });

                    this.classList.add('main-selected');
                    this.style.border = '3px solid #007bff';
                    selectedMainIndex = this.dataset.index;
                });

                previewContainer.appendChild(imgElement);
            };
            reader.readAsDataURL(file); // 파일을 DataURL로 읽기
        });
    } else {
        // ✅ 이미지가 없는 경우 기본 이미지 삽입
        let defaultImg = document.createElement("img");
        defaultImg.src = "/images/default-thumbnail.png";
        defaultImg.alt = "기본 이미지";
        defaultImg.classList.add("img-thumbnail", "m-2");
        defaultImg.style.width = "150px";
        defaultImg.style.height = "150px";
        previewContainer.appendChild(defaultImg);
    }
}