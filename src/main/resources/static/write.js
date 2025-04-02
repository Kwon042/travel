document.getElementById("image").addEventListener("change", function(event) {
    let files = event.target.files;
    if (files.length === 0) return;

    let previewContainer = document.getElementById('imagePreview');
    previewContainer.innerHTML = ''; // 기존 미리보기 초기화

    let imageUrlsInput = document.getElementById("imageUrls");
    let uploadedUrls = [];

    let formData = new FormData();

    // reviewBoardId 가져오기 (미리 선언)
    let reviewBoardIdInput = document.querySelector("input[name='id']");
    let reviewBoardId = reviewBoardIdInput ? reviewBoardIdInput.value : null;

    if (reviewBoardId) {
        formData.append("reviewBoardId", reviewBoardId);
    }

    // 모든 파일을 한 번에 추가
    Array.from(files).forEach(file => {
        formData.append("files[]", file);
    });

    fetch("/api/image/board/upload", {
        method: "POST",
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.imageUrls) {
            console.log("Uploaded Image URL List:", data.imageUrls);
            uploadedUrls = data.imageUrls;
            imageUrlsInput.value = uploadedUrls.join(",");

            // 화면에 이미지 미리보기 추가
            uploadedUrls.forEach(url => {
                let imgElement = document.createElement("img");
                imgElement.src = url;
                imgElement.classList.add("img-thumbnail", "m-2");
                imgElement.style.width = "150px";
                imgElement.style.height = "150px";
                previewContainer.appendChild(imgElement);
            });
        }
    })
    .catch(error => console.error("Image Upload Failed:", error));
});

