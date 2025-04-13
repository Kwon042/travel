document.addEventListener("DOMContentLoaded", function() {
    const submitButton = document.getElementById("submitCommentButton");
    const reviewBoardId = document.getElementById("commentSection").getAttribute("data-review-board-id");

    // 댓글 작성
    submitButton.addEventListener("click", function() {
        submitComment(reviewBoardId);
    });
    // 수정/삭제 버튼 이벤트 위임
    const commentSection = document.querySelector(".comments-section");

    if (commentSection) {
        commentSection.addEventListener("click", function(event) {
            const target = event.target;

            if (target.classList.contains("delete-button")) {
                const commentId = target.getAttribute("data-id");
                deleteComment(commentId);
            }

            if (target.classList.contains("edit-button")) {
                const commentId = target.getAttribute("data-id");
                editComment(commentId);
            }
        });
    }
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

function deleteComment(commentId) {
    if (!confirm("정말 이 댓글을 삭제하시겠습니까?")) return;

    fetch(`/comments/${commentId}`, {
        method: 'DELETE',
    })
    .then(response => {
        if (response.ok) {
            location.reload(); // 삭제 후 새로고침
        } else {
            console.error('댓글 삭제 실패');
        }
    })
    .catch(error => {
        console.error('에러:', error);
    });
}

function editComment(commentId) {
    const commentEl = document.querySelector(`.edit-button[data-id="${commentId}"]`).closest('.comment');
    const contentEl = commentEl.querySelector('.comment-body');
    const originalText = contentEl.innerText.trim();

    // 이미 수정 중이면 return
    if (commentEl.querySelector('textarea')) return;

    // 기존 내용 숨기기
    contentEl.style.display = "none";

    // 수정 폼 생성
    const formDiv = document.createElement('div');
    formDiv.className = "edit-form";

    const textarea = document.createElement('textarea');
    textarea.className = "form-control mb-2";
    textarea.value = originalText;

    const saveBtn = document.createElement('button');
    saveBtn.className = "btn btn-sm btn-primary me-2";
    saveBtn.innerText = "저장";

    const cancelBtn = document.createElement('button');
    cancelBtn.className = "btn btn-sm btn-secondary";
    cancelBtn.innerText = "취소";

    // 저장
    saveBtn.addEventListener('click', () => {
        const newContent = textarea.value.trim();
        if (newContent === "" || newContent === originalText) {
            cancelBtn.click(); // 취소와 동일 처리
            return;
        }

        fetch(`/comments/${commentId}`, {
            method: 'PUT',
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ content: newContent })
        })
        .then(response => {
            if (response.ok) {
                location.reload(); // 새로고침
            } else {
                alert("수정 실패");
            }
        })
        .catch(error => console.error("에러:", error));
    });

    // 취소
    cancelBtn.addEventListener('click', () => {
        formDiv.remove();
        contentEl.style.display = "";
    });

    formDiv.appendChild(textarea);
    formDiv.appendChild(saveBtn);
    formDiv.appendChild(cancelBtn);

    commentEl.querySelector('.comment-body').after(formDiv);
}

//function editComment(commentId) {
//    const commentEl = document.querySelector(`.edit-button[data-id="${commentId}"]`).closest('.comment');
//    const contentEl = commentEl.querySelector('.comment-body p');
//    const originalText = contentEl.textContent;
//
//    const newContent = prompt("댓글을 수정하세요:", originalText);
//    if (newContent === null || newContent.trim() === "" || newContent === originalText) return;
//
//    fetch(`/comments/${commentId}`, {
//        method: 'PUT',
//        headers: {
//            "Content-Type": "application/json"
//        },
//        body: JSON.stringify({ content: newContent })
//    })
//    .then(response => {
//        if (response.ok) {
//            location.reload();
//        } else {
//            console.error('댓글 수정 실패');
//        }
//    })
//    .catch(error => {
//        console.error('에러:', error);
//    });
//}