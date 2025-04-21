document.addEventListener("DOMContentLoaded", () => {
    const urlParams = new URLSearchParams(window.location.search);
    const region = urlParams.get('region') || "전체";
    const regionTitle = document.getElementById('region-title');
    const writeButton = document.getElementById('writeButton');

    if (regionTitle) regionTitle.textContent = `${region} 게시판`;
    if (writeButton) writeButton.href = `/board/write?region=${region}&boardType=reviewBoard`;

    document.querySelectorAll('.likes').forEach(el => {
        const boardId = el.id.split('_')[1];
        if (boardId) {
            updateLikeCount(boardId);
            checkLikeStatus(boardId);

            // 하트 클릭 이벤트 추가
            const likeIcon = document.getElementById(`like-icon-${boardId}`);
            if (likeIcon) {
                likeIcon.addEventListener('click', () => handleLikeClick(boardId));
            }
        }
    });

    const boardElement = document.getElementById('board-details');
    if (boardElement) {
        const boardId = boardElement.dataset.boardId;
        updateCommentCount(boardId);

        document.getElementById("deleteButton")?.addEventListener("click", () => deleteReviewBoard(boardId));
    } else {
        console.error('board-details 요소를 찾을 수 없습니다.');
    }

    window.csrfToken = document.querySelector("input[name='_csrf']")?.value || null;
});

function checkLikeStatus(boardId) {
    fetch(`/reviewBoard/likes/${boardId}/status`)
        .then(res => res.ok ? res.json() : Promise.reject("상태 확인 실패"))
        .then(hasLiked => updateHeartIcon(boardId, hasLiked))
        .catch(err => console.error("좋아요 상태 확인 실패:", err));
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

function handleLikeClick(boardId) {
    const likeIcon = document.getElementById(`like-icon-${boardId}`);
    const isLiked = likeIcon?.textContent === '💜';  // 현재 상태 확인
    const method = isLiked ? "DELETE" : "POST";  // 삭제 또는 추가

    // 1. UI에서 좋아요 상태를 즉시 반영 (클릭 즉시 하트 아이콘을 토글)
    updateHeartIcon(boardId, !isLiked); // 하트 아이콘 상태 토글
    updateLikeCount(boardId);

    // 2. 서버 요청을 보내서 실제 좋아요 상태를 반영
    fetch(`/reviewBoard/likes/${boardId}/status`)  // 좋아요 상태 확인
        .then(res => res.json())
        .then(hasLiked => {
            if (hasLiked) {
                // 이미 좋아요가 눌려 있으면 DELETE 요청
                fetch(`/reviewBoard/likes/${boardId}`, {
                    method: "DELETE",
                    headers: {
                        "Content-Type": "application/json",
                        'X-CSRF-TOKEN': window.csrfToken
                    }
                })
                .then(res => {
                    if (res.ok) {
                        updateHeartIcon(boardId, false);
                        updateLikeCount(boardId);
                    } else {
                        console.error("좋아요 삭제 실패");
                    }
                });
            } else {
                // 좋아요가 눌려 있지 않으면 POST 요청
                fetch(`/reviewBoard/likes/${boardId}`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        'X-CSRF-TOKEN': window.csrfToken
                    }
                })
                .then(res => {
                    if (res.ok) {
                        updateHeartIcon(boardId, true);
                        updateLikeCount(boardId);
                    } else {
                        console.error("좋아요 추가 실패");
                    }
                });
            }
        })
        .catch(err => {
            console.error("좋아요 상태 확인 실패:", err);
        });
}

function updateHeartIcon(boardId, hasLiked) {
    const icon = document.getElementById(`like-icon-${boardId}`);
    if (icon) icon.textContent = hasLiked ? '💜' : '🤍';
}

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
