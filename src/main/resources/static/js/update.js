// 폼 제출 시 에디터 내용 저장
document.querySelector("form").addEventListener("submit", function() {
    const content = editor.getMarkdown(); // 에디터에서 내용 가져오기
    document.querySelector('input[name="content"]').value = content; // 숨겨진 필드에 저장
});

// 수정 시 기존 이미지도 미리보기로 보이게 하는 함수
function loadExistingImages(images) {
    let previewContainer = document.getElementById('imagePreview');
    previewContainer.innerHTML = ''; // 기존 미리보기 초기화

    images.forEach(url => {
        let imgElement = document.createElement("img");
        imgElement.src = url;
        imgElement.classList.add("img-thumbnail", "m-2");
        imgElement.style.width = "150px";
        imgElement.style.height = "150px";
        previewContainer.appendChild(imgElement);
    });
}

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
      reader.readAsDataURL(file);
    });
  }
}
