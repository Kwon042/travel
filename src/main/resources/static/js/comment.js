document.addEventListener("DOMContentLoaded", function() {
    const submitButton = document.getElementById("submitCommentButton");
    const reviewBoardId = document.getElementById("commentSection").getAttribute("data-review-board-id");

    // 댓글 작성
    submitButton.addEventListener("click", function() {
        submitComment(reviewBoardId);
    });
    // 수정/삭제/답글 버튼 이벤트 위임
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
                const isReply = target.closest('.reply-form') !== null;
                editComment(commentId, isReply);
            }

            // 답글 버튼 클릭 (토글 기능 구현)
            if (target.classList.contains("reply-button")) {
                const commentId = target.getAttribute("data-comment-id");
                const replyForm = document.getElementById(`replyForm_${commentId}`);

                // 이미 active 클래스가 있다면 제거하고, 없다면 추가
                if (replyForm.classList.contains("active")) {
                    replyForm.classList.remove("active"); // 폼 닫기
                } else {
                    replyForm.classList.add("active"); // 폼 열기
                }
            }

            // 답글 작성 버튼 클릭
            if (target.classList.contains("submit-reply-button")) {
                const parentId = target.getAttribute("data-parent-id");
                submitReply(reviewBoardId, parentId);
            }

            // 답글 취소 버튼 클릭
            if (target.classList.contains("cancel-reply-button")) {
                const commentId = target.getAttribute("data-comment-id");
                const replyForm = document.getElementById(`replyForm_${commentId}`);
                replyForm.classList.remove("active"); // 폼 닫기
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
    let contentEl;

    // 댓글인지 대댓글인지 확인
    if (commentEl.classList.contains('reply')) {
        // 대댓글인 경우
        contentEl = commentEl.querySelector('.reply-body p');
    } else {
        // 댓글인 경우
        contentEl = commentEl.querySelector('.comment-body p');
    }

    const originalText = contentEl.innerText.trim();

    // 이미 수정 중이면 return
    if (commentEl.querySelector('.edit-form')) {
        // 이미 수정 폼이 열려있으면 닫기
        const existingForm = commentEl.querySelector('.edit-form');
        const saveBtn = existingForm.querySelector('.btn-primary');
        const cancelBtn = existingForm.querySelector('.btn-secondary');

        // 이벤트 리스너 제거
        saveBtn.removeEventListener('click', saveBtn.eventListener);
        cancelBtn.removeEventListener('click', cancelBtn.eventListener);

        existingForm.remove();
        contentEl.style.display = "";
        return;
    }

    // 수정 폼 생성
    const formDiv = document.createElement('div');
    formDiv.className = "edit-form";

    const textarea = document.createElement('textarea');
    textarea.className = "form-control mb-2";
    textarea.value = originalText;

    const saveBtn = document.createElement('button');
    saveBtn.className = "edit-save-button";
    saveBtn.innerText = "저장";

    const cancelBtn = document.createElement('button');
    cancelBtn.className = "edit-cancel-button";
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
    });

    formDiv.appendChild(textarea);
    formDiv.appendChild(saveBtn);
    formDiv.appendChild(cancelBtn);

    formDiv.classList.add('active');

    if (commentEl.classList.contains('reply')) {
        // 대댓글인 경우
        commentEl.querySelector('.reply-body').after(formDiv);
    } else {
        // 댓글인 경우
        commentEl.querySelector('.comment-body').after(formDiv);
    }
}

// 답글 제출
function submitReply(reviewBoardId, parentId) {
    const content = document.querySelector(`#replyForm_${parentId} .reply-content`).value;

    fetch(`/comments/${reviewBoardId}?content=${encodeURIComponent(content)}&parentId=${parentId}`, {
        method: 'POST',
    })
    .then(response => {
        if (response.ok) {
            location.reload(); // 페이지를 새로 고침
        } else {
            console.error('답글 추가 실패');
        }
    })
    .catch(error => {
        console.error('에러:', error);
    });
}



//수정폼이 위에서 뿅
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