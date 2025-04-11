document.addEventListener("DOMContentLoaded", function() {
    const submitButton = document.getElementById("submitCommentButton");
    const reviewBoardId = document.getElementById("commentSection").getAttribute("data-review-board-id");

    // 버튼 클릭 시 submitComment 함수 호출
    submitButton.addEventListener("click", function() {
        submitComment(reviewBoardId);
    });
});

function submitComment(reviewBoardId) {
    const content = document.querySelector('#commentContent').value;
    console.log(`URL: /comments/${reviewBoardId}?content=${encodeURIComponent(content)}`);

    fetch(`/comments/${reviewBoardId}?content=${encodeURIComponent(content)}`, {
        method: 'POST',
    })
    .then(response => {
        if (response.ok) {
            location.reload(); // 페이지를 새로 고침
        } else {
            console.error('댓글 추가 실패');
        }
    })
    .catch(error => {
        console.error('에러:', error);
    });
}