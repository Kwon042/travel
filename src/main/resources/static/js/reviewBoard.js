document.addEventListener("DOMContentLoaded", function() {
        const urlParams = new URLSearchParams(window.location.search);
        const region = urlParams.get('region') || "전체"; // 기본값 설정

        // 'region-title'의 내용을 업데이트
        document.getElementById('region-title').textContent = `${region} 게시판`;

        // 'writeButton'이 있을 경우 그것의 href 속성 업데이트
        const writeButton = document.getElementById('writeButton');
        if (writeButton) {
            writeButton.href = `/board/write?region=${region}&boardType=reviewBoard`;
        }
        // 페이지가 로드될 때 좋아요 수를 가져와서 업데이트
        document.querySelectorAll('.likes').forEach(function(likeElement) {
            const boardId = likeElement.id.split('_')[1];  // likes_{boardId}에서 boardId 추출
            if (boardId) {
                updateLikeCount(boardId, likeElement);
            }
        });
        // 페이지가 로드될 때 댓글 수를 가져와서 업데이트
        document.querySelectorAll('.btn-light').forEach(function(commentButton) {
            const boardId = commentButton.querySelector('span').id.split('_')[1]; // 댓글 수를 보여주는 span의 ID에서 boardId 추출
            if (boardId) {
                updateCommentCount(boardId, commentButton);
            }
        });
});

// 좋아요 수를 서버에서 가져와서 갱신하는 함수
function updateLikeCount(boardId, likeElement) {
    fetch(`/reviewBoard/likes/${boardId}/count`)
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch like count');
        }
        return response.json();
    })
    .then(count => {
        if (typeof count === 'number') {
            likeElement.textContent = `♡ ${count}`;  // 좋아요 수 업데이트
        } else {
            console.error('Invalid response format:', count);
        }
    })
    .catch(error => {
        console.error('Error fetching like count:', error);
        likeElement.textContent = '♡ 0';  // 에러가 발생하면 기본 값으로 설정
    });
}

// 좋아요 목록 표시 함수
function showLikeList(boardId) {
    fetch(`/reviewBoard/likes/${boardId}`)
    .then(response => response.json())
    .then(likeUsers => {
        const userList = likeUsers.map(user => `<li>${user.nickname}</li>`).join('');
        alert(`이 게시글을 좋아요한 사용자들:\n${userList}`);
    })
    .catch(error => console.error('Error fetching likes:', error));
}

function updateCommentCount(reviewBoardId) {
    // 서버에 댓글 수 요청
    fetch(`/comments/${reviewBoardId}`)  // 댓글 수 가져오기 API 호출
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            // 댓글 수를 업데이트
            const commentCountElement = document.getElementById(`commentCount_${reviewBoardId}`);
            if (commentCountElement) {
                commentCountElement.textContent = data.commentsCount || 0; // 댓글 수를 설정 (없으면 0)
            }
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}
