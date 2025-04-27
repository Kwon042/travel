document.addEventListener("DOMContentLoaded", function() {
    const boardId = document.getElementById('board-details').getAttribute('data-board-id');

    // Toast UI Viewer로 마크다운 렌더링
    const markdownContent = ""; // 템플릿에서 내용을 받아올 부분
    const viewer = new toastui.Editor.factory({
      el: document.querySelector('#viewer'),
      viewer: true,
      initialValue: markdownContent
    });

    // 댓글 수 갱신
    updateCommentCount(boardId);

    // 좋아요 관련 버튼 초기화
    initializeLikeButtons();

    // 게시글 세부 정보 있을 경우 댓글 수 갱신
    initializeBoardDetails();
});

function initializeLikeButtons() {
    document.querySelectorAll('.like-icon').forEach(likeIcon => {
        const boardId = likeIcon.getAttribute('data-id');
        if (boardId) {
            updateLikeCount(boardId);     // 좋아요 수 갱신
            checkLikeStatus(boardId);     // 좋아요 상태 확인

            // 하트 클릭 이벤트 추가
            likeIcon.addEventListener('click', () => handleLikeClick(boardId));
        }
    });
}

function initializeBoardDetails() {
    const boardElement = document.getElementById('board-details');
    if (boardElement) {
        const boardId = boardElement.dataset.boardId;
        updateCommentCount(boardId); // 댓글 수 갱신
    } else {
        console.error('board-details 요소를 찾을 수 없습니다.');
    }
}

function checkLikeStatus(boardId) {
    fetch(`/reviewBoard/likes/${boardId}/status`)
        .then(res => res.ok ? res.json() : Promise.reject("상태 확인 실패"))
        .then(hasLiked => updateHeartIcon(boardId, hasLiked))
        .catch(err => console.error("좋아요 상태 확인 실패:", err));
}

function handleLikeClick(boardId) {
    const likeIcon = document.getElementById(`like-icon-${boardId}`);
    const isLiked = likeIcon?.textContent === '💜'; // 현재 상태 확인

    const method = isLiked ? "DELETE" : "POST";

    // 1. UI 하트만 즉시 토글
    updateHeartIcon(boardId, !isLiked);

    // 2. 서버에 바로 좋아요 추가/삭제 요청
    fetch(`/reviewBoard/likes/${boardId}`, {
        method,
        headers: {
            "Content-Type": "application/json",
            'X-CSRF-TOKEN': window.csrfToken
        }
    })
    .then(res => {
        if (res.ok) {
            updateLikeCount(boardId); // 성공했을 때 좋아요 수 업데이트
        } else {
            console.error(`좋아요 ${isLiked ? '삭제' : '추가'} 실패`);
            updateHeartIcon(boardId, isLiked); // 실패하면 원래대로 롤백
        }
    })
    .catch(err => {
        console.error('좋아요 요청 실패:', err);
        updateHeartIcon(boardId, isLiked); // 실패하면 원래대로 롤백
    });
}

function updateHeartIcon(boardId, hasLiked) {
    const icon = document.getElementById(`like-icon-${boardId}`);
    if (icon) {
        icon.textContent = hasLiked ? '💜' : '🤍';
    }
}

function updateLikeCount(boardId) {
    fetch(`/reviewBoard/likes/${boardId}/count`)
        .then(res => res.ok ? res.json() : Promise.reject('Failed to fetch like count'))
        .then(count => {
            const el = document.getElementById(`likeCount_${boardId}`);
            if (el) el.textContent = count;
        })
        .catch(err => {
            console.error('Error fetching like count:', err);
            const el = document.getElementById(`likeCount_${boardId}`);
            if (el) el.textContent = '0';
        });
}

function updateCommentCount(boardId) {
    fetch(`/comments/${boardId}/count`)
        .then(res => res.ok ? res.json() : Promise.reject("댓글 수 불러오기 실패"))
        .then(count => {
            const el = document.getElementById(`commentCount_${boardId}`);
            if (el) el.textContent = count;
        })
        .catch(err => console.error("댓글 수 갱신 오류:", err));
}

function goToComments(reviewBoardId) {
    window.location.href = `/comments/${reviewBoardId}/show`;
}
