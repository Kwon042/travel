document.querySelector("form").addEventListener("submit", function(event) {
    event.preventDefault(); // 기본 폼 제출 방지

    let submitButton = document.querySelector("button[type='submit']");
    submitButton.disabled = true; // 중복 제출 방지

    let form = event.target;
    let formData = new FormData(form);
    let csrfToken = document.querySelector("input[name='_csrf']").value;

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
            // 게시글 저장 성공 시 reviewBoardId 가져오기
            let reviewBoardId = data.boardId; // 서버 응답에서 ID를 받아야 합니다.

            // 이미지 업로드 로직 조건 처리 (선택한 파일이 있는지 확인)
            let files = document.getElementById("image").files;
            if (files.length > 0) { // 이미지 파일이 선택된 경우
                let imageFormData = new FormData();
                Array.from(files).forEach(file => {
                    imageFormData.append("files[]", file);
                });
                imageFormData.append("reviewBoardId", reviewBoardId); // 게시글 ID 추가

                return fetch("/api/image/board/upload", {
                    method: "POST",
                    body: imageFormData
                }).then(imageResponse => {
                    if (!imageResponse.ok) {
                        throw new Error("이미지 업로드 실패");
                    }
                    return imageResponse.json();
                }).then(imageData => {
                    console.log("이미지 업로드 성공:", imageData.message);
                });
            } else {
                // 이미지가 없으면 바로 다음 단계
                alert("게시글이 저장되었습니다.");
            }
        } else {
            alert("게시글 저장 실패");
        }
    })
    .then(() => {
        // 최종적으로 게시판 목록으로 리다이렉트
        window.location.href = "/board/reviewBoard";
    })
    .catch(error => {
        console.error(error);
        submitButton.disabled = false; // 에러 발생 시 버튼 다시 활성화
    });
});

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
                previewContainer.appendChild(imgElement);
            };
            reader.readAsDataURL(file); // 파일을 DataURL로 읽기
        });
    }
}