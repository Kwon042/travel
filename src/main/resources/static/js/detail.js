// DOMContentLoaded 이벤트 리스너로 페이지 로딩 후 초기화 작업
document.addEventListener("DOMContentLoaded", () => {
    const urlParams = new URLSearchParams(window.location.search);
    const region = urlParams.get('region') || "전체";
    const regionTitle = document.getElementById('region-title');
    const writeButton = document.getElementById('writeButton');

    // 지역 제목 설정
    if (regionTitle) regionTitle.textContent = `${region} 게시판`;
    if (writeButton) writeButton.href = `/board/write?region=${region}&boardType=reviewBoard`;

    // 좋아요 버튼 초기화 및 이벤트 핸들러 설정
    initializeLikeButtons();

    // 게시글 세부 정보 있을 경우 댓글 수 갱신
    initializeBoardDetails();

    // CSRF 토큰 설정
    window.csrfToken = document.querySelector("input[name='_csrf']")?.value || null;
});

// 좋아요 관련 버튼 초기화
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

// 게시글 세부 정보가 있을 경우 댓글 수 갱신
function initializeBoardDetails() {
    const boardElement = document.getElementById('board-details');
    if (boardElement) {
        const boardId = boardElement.dataset.boardId;
        updateCommentCount(boardId); // 댓글 수 갱신

        // 게시글 삭제 버튼 이벤트
        document.getElementById("deleteButton")?.addEventListener("click", () => deleteReviewBoard(boardId));
    } else {
        console.error('board-details 요소를 찾을 수 없습니다.');
    }
}

// 좋아요 상태를 확인하여 하트 아이콘 상태 업데이트
function checkLikeStatus(boardId) {
    fetch(`/reviewBoard/likes/${boardId}/status`)
        .then(res => res.ok ? res.json() : Promise.reject("상태 확인 실패"))
        .then(hasLiked => updateHeartIcon(boardId, hasLiked))
        .catch(err => console.error("좋아요 상태 확인 실패:", err));
}

// 좋아요 클릭 시 처리
function handleLikeClick(boardId) {
    const likeIcon = document.getElementById(`like-icon-${boardId}`);
    const isLiked = likeIcon?.textContent === '💜'; // 현재 상태 확인

    // 1. UI 하트만 즉시 토글
    updateHeartIcon(boardId, !isLiked);

    // 2. 서버에 실제 좋아요 상태 요청
    fetch(`/reviewBoard/likes/${boardId}/status`)
        .then(res => res.json())
        .then(hasLiked => {
            const method = hasLiked ? "DELETE" : "POST";
            fetch(`/reviewBoard/likes/${boardId}`, {
                method,
                headers: {
                    "Content-Type": "application/json",
                    'X-CSRF-TOKEN': window.csrfToken
                }
            })
            .then(res => {
                if (res.ok) {
                    updateLikeCount(boardId); // 서버 성공했을 때만 좋아요 수 업데이트
                } else {
                    console.error(`좋아요 ${hasLiked ? '삭제' : '추가'} 실패`);
                    updateHeartIcon(boardId, hasLiked); // 실패하면 아이콘 롤백
                }
            })
            .catch(err => {
                console.error('좋아요 요청 실패:', err);
                updateHeartIcon(boardId, hasLiked); // 실패하면 아이콘 롤백
            });
        })
        .catch(err => {
            console.error('좋아요 상태 확인 실패:', err);
            updateHeartIcon(boardId, isLiked); // 실패하면 아이콘 원래대로
        });
}

// 하트 아이콘 상태 업데이트
function updateHeartIcon(boardId, hasLiked) {
    const icon = document.getElementById(`like-icon-${boardId}`);
    if (icon) {
        icon.textContent = hasLiked ? '💜' : '🤍';
    }
}

// 좋아요 수 업데이트
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

// 게시글 삭제 처리
function deleteReviewBoard(boardId) {
    if (!confirm("정말로 이 게시글을 삭제하시겠습니까?")) return;

    fetch(`/board/reviewBoard/delete/${boardId}`, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            'X-CSRF-TOKEN': window.csrfToken
        }
    })
    .then(res => {
        if (res.ok) {
            alert("게시글이 삭제되었습니다.");
            window.location.href = "/board/reviewBoard";
        } else {
            alert("게시글 삭제에 실패했습니다.");
        }
    })
    .catch(err => console.error("Error deleting review board:", err));
}

// 댓글 수 갱신
function updateCommentCount(boardId) {
    fetch(`/comments/${boardId}/count`)
        .then(res => res.ok ? res.json() : Promise.reject("댓글 수 불러오기 실패"))
        .then(count => {
            const el = document.getElementById(`commentCount_${boardId}`);
            if (el) el.textContent = count;
        })
        .catch(err => console.error("댓글 수 갱신 오류:", err));
}

// 댓글 보기 페이지로 이동
function goToComments(reviewBoardId) {
    window.location.href = `/comments/${reviewBoardId}/show`;
}
