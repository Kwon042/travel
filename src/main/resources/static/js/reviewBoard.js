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

// 댓글 수를 서버에서 가져와서 갱신하는 함수
function updateCommentCount(boardId) {
    fetch(`/comments/${boardId}/count`)
        .then(response => {
            if (!response.ok) throw new Error("댓글 수를 불러오는데 실패했습니다.");
            return response.json();
        })
        .then(count => {
            const countElement = document.getElementById("commentCount_" + boardId);
            if (countElement) {
                countElement.textContent = count;
            }
        })
        .catch(error => {
            console.error("댓글 수 갱신 오류:", error);
        });
}
