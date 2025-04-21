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
          updateLikeCount(boardId);
          checkUserLikeStatus(boardId);  // 하트 색상도 초기화
      }
  });

  // 게시글 ID 가져오기
  const boardElement = document.getElementById('board-details');
    if (boardElement) {
        const boardId = boardElement.dataset.boardId;  // 게시글 ID

        // 댓글 수 갱신 추가
        updateCommentCount(boardId);

        // 삭제 버튼 이벤트 리스너 추가
        const deleteButton = document.getElementById("deleteButton");
        if (deleteButton) {
            deleteButton.addEventListener("click", function() {
                deleteReviewBoard(boardId);
            });
        }
    } else {
        console.error('board-details 요소를 찾을 수 없습니다.');
    }
    // CSRF 토큰 가져오기
    let csrfTokenInput = document.querySelector("input[name='_csrf']");
    const csrfToken = csrfTokenInput ? csrfTokenInput.value : null; // CSRF 토큰 값 설정

    // CSRF 토큰을 deleteReviewBoard에 전달
    window.csrfToken = csrfToken; // 전역에서 사용할 수 있도록 설정
  });

// 좋아요 상태 확인해서 하트 색상 맞추는 함수
function checkUserLikeStatus(boardId) {
    fetch(`/reviewBoard/likes/${boardId}/status`)
        .then(response => {
            if (!response.ok) throw new Error("상태 확인 실패");
            return response.json();
        })
        .then(hasLiked => {
            const likeIcon = document.getElementById(`like-icon-${boardId}`);
            if (likeIcon) {
                likeIcon.textContent = hasLiked ? '💜' : '🤍';
            }
        })
        .catch(error => {
            console.error("좋아요 상태 확인 실패:", error);
        });
}


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
        document.getElementById(`likeCount_${boardId}`).textContent = count;
    } else {
        console.error('Invalid response format:', count);
    }
  })
  .catch(error => {
    console.error('Error fetching like count:', error);
    document.getElementById(`like-count-${boardId}`).textContent = '0';  // 에러가 발생하면 기본 값으로 설정
  });
}

function handleLikeClick(boardId) {
    const likeIcon = document.getElementById(`like-icon-${boardId}`);
    const isLiked = likeIcon && likeIcon.textContent === '💜';

    const method = isLiked ? "DELETE" : "POST";

    fetch(`/reviewBoard/likes/${boardId}`, {
        method: method,
        headers: {
            "Content-Type": "application/json",
            'X-CSRF-TOKEN': window.csrfToken
        }
    })
    .then(response => {
        if (!response.ok) throw new Error(isLiked ? "좋아요 취소 실패" : "좋아요 실패");
        return response.text();
    })
    .then(() => {
        if (likeIcon) {
            likeIcon.textContent = isLiked ? '🤍' : '💜'; // 보라색 하트(💜)로 변경
        }
        updateLikeCount(boardId);  // 좋아요 수 최신화
    })
    .catch(error => {
        console.error("좋아요 처리 에러:", error);
    });
}


// 댓글 입력 창으로 전환하는 함수
function goToComments(reviewBoardId) {
    window.location.href = `/comments/${reviewBoardId}/show`; // 댓글 섹션으로 이동
}

function deleteReviewBoard(boardId) {
    if (!confirm("정말로 이 게시글을 삭제하시겠습니까?")) {
        return;
    }

    fetch(`/board/reviewBoard/delete/${boardId}`, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            'X-CSRF-TOKEN': window.csrfToken
        }
    })
    .then(response => {
        if (response.ok) {
            alert("게시글이 삭제되었습니다.");
            window.location.href = "/board/reviewBoard";
        } else {
            alert("게시글 삭제에 실패했습니다.");
        }
    })
    .catch(error => console.error("Error deleting review board:", error));
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