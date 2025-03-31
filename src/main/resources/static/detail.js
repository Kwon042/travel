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

  // 페이지가 로드될 때 모든 좋아요 수를 가져와서 업데이트
  document.querySelectorAll('.likes').forEach(function(likeElement) {
      const boardId = likeElement.id.split('_')[1];  // likes_{boardId}에서 boardId 추출
      if (boardId) {
          updateLikeCount(boardId, likeElement);  // 좋아요 수를 가져와서 갱신
      }
  });
  // 댓글 목록 로드
  const boardElement = document.getElementById('board-details');
  if (boardElement) {
      const boardId = boardElement.getAttribute('data-board-id');  // 게시글 ID
      loadComments(boardId); // 댓글 목록 로드
  } else {
      console.error('board-details 요소를 찾을 수 없습니다.');
  }
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
            document.getElementById(`like-count-${boardId}`).textContent = count;  // 좋아요 수 업데이트
        } else {
            console.error('Invalid response format:', count);
        }
      })
      .catch(error => {
        console.error('Error fetching like count:', error);
        document.getElementById(`like-count-${boardId}`).textContent = '0';  // 에러가 발생하면 기본 값으로 설정
      });
  }

  // 댓글을 서버에 추가하는 함수
  function addCommentToBoard(reviewBoardId) {
      const commentContent = document.getElementById('comment-content').value;

      fetch(`/comments/${reviewBoardId}?content=${commentContent}`, {
          method: 'POST',
          headers: {
              'Content-Type': 'application/json'
          }
      })
      .then(response => response.json())
      .then(data => {
          alert(data);
          loadComments(reviewBoardId); // 댓글 목록 새로고침
      })
      .catch(error => console.error('Error:', error));
  }

  // 댓글 목록을 서버에서 로드하는 함수
  function loadComments(reviewBoardId) {
      fetch(`/comments/${reviewBoardId}`)
      .then(response => response.json())
      .then(comments => {
          const commentList = document.getElementById('comment-list');
          commentList.innerHTML = ''; // 기존 댓글 목록 초기화

          comments.forEach(comment => {
              const commentElement = document.createElement('div');
              commentElement.classList.add('comment');
              commentElement.innerHTML = `
                  <strong>${comment.user.nickname}</strong>: ${comment.content}
                  <br><small>${comment.createdAt}</small>
              `;
              commentList.appendChild(commentElement);
          });
      })
      .catch(error => console.error('Error loading comments:', error));
  }