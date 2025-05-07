document.addEventListener('DOMContentLoaded', function () {
    const attractionCards = document.querySelectorAll('.attraction-card');

    attractionCards.forEach(card => {
        card.addEventListener('click', function () {
            const contentId = card.getAttribute('data-content-id');
            const contentTypeId = card.getAttribute('data-content-type-id');
            const areaCode = card.getAttribute('data-area-code');

            fetch(`/api/attraction/detail/${contentId}/${contentTypeId}?areaCode=${areaCode}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('서버 요청 실패');
                    }
                    return response.json();
                })
                .then(detail => {
                    console.log("detail data:", detail);

                    // firstimage가 없으면 지역기반 API로 이미지를 찾는 함수 호출
                    if (!detail.firstimage || detail.firstimage.trim() === '') {
                        fetchImageFromRegionAPI(contentId, contentTypeId, areaCode)
                            .then(imageUrl => {
                                detail.firstimage = imageUrl || '/images/no-image.png';
                                showDetailModal(detail);
                            })
                            .catch(error => {
                                console.error('Error fetching image from region API:', error);
                                detail.firstimage = '/images/no-image.png'; // 기본 이미지로 설정
                                showDetailModal(detail);
                            });
                    } else {
                        showDetailModal(detail);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('상세 정보를 불러오는 데 실패했습니다.');
                });
        });
    });
});

// 지역기반 API에서 이미지를 가져오는 함수
function fetchImageFromRegionAPI(contentId, contentTypeId, areaCode) {
    return fetch(`/api/attraction/region-image/${contentId}/${contentTypeId}?areaCode=${areaCode}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('지역기반 API 요청 실패');
            }
            return response.json();
        })
        .then(data => {
            if (data && data.firstimage) {
                return data.firstimage; // 지역기반 API에서 가져온 이미지 URL 반환
            }
            return null; // 이미지가 없으면 null 반환
        })
        .catch(error => {
            console.error('Error fetching image from region API:', error);
            return null; // 에러 발생 시 null 반환
        });
}

function showDetailModal(detail) {
    const modalBody = document.getElementById('modalBody');

    // infoList 처리
    let infoHtml = getInfoHtml(detail.infoList);

    const emoji = getEmojiByContentTypeId(String(detail.contentTypeId));

    // 모달 내용 설정
    setModalContent(modalBody, detail, emoji, infoHtml);

    // 부트스트랩 모달 띄우기
    const modal = new bootstrap.Modal(document.getElementById('attractionModal'));
    modal.show();

    // 즐겨찾기 이미지 클릭 시 상태 변경
    setBookmarkToggleEvent();
}

// infoList HTML 생성 함수
function getInfoHtml(infoList) {
    let infoHtml = '';
    if (Array.isArray(infoList) && infoList.length > 0) {
        infoList.forEach(info => {
            infoHtml += `
                <div>
                    <hr style="border: none; border-top: 1px solid black; margin: 10px 0;">
                    <strong>${info.infoName || '정보 없음'}</strong>
                    <p>${info.infoText || '정보 없음'}</p>
                </div>
            `;
        });
    } else {
        infoHtml = '<p>추가 정보가 없습니다.</p>';
    }
    return infoHtml;
}

function getEmojiByContentTypeId(contentTypeId) {
    const emojiMap = {
        "12": "🗽",  // 관광지
        "14": "🏕️",  // 문화시설
        "15": "🎡",  // 축제
        "28": "🤿",  // 레포츠
        "32": "🏨",  // 숙박
        "38": "🛒",  // 쇼핑
        "39": "🍽️",  // 음식점
    };
    return emojiMap[contentTypeId] || "";
}

// 모달 내용 설정 함수
function setModalContent(modalBody, detail, emoji, infoHtml) {
    modalBody.innerHTML = `
        <div class="modal-main-header-container">
            <h4 class="modal-main-title-text">${emoji} ${detail.title || '제목 없음'}</h4>
            <div class="main-image-group">
                <img id="bookmarkIcon" class="main-bookmark-icon"
                     src="/images/bookmark-white_icon.png"
                     data-attraction-id="${detail.contentId}"
                     data-content-type-id="${detail.contentTypeId}"
                     data-area-code="${detail.areaCode}"
                     data-firstimage="${detail.firstimage || ''}"
                     alt="즐겨찾기">
                <img src="${detail.firstimage || '/images/no-image.png'}" alt="이미지" class="detail-image">
            </div>
        </div>
        <hr style="border: none; border-top: 2px solid black; margin: 10px 0;">
        <p style="margin: 0;"><strong class="highlight">주소</strong></p>
        <p style="margin: 0;">${detail.addr1 || '정보 없음'}</p>
        <hr style="border: none; border-top: 1px solid black; margin: 10px 0;">

        <p style="margin: 0;"><strong class="highlight">전화번호</strong></p>
        <p style="margin: 0;">${detail.tel || '정보 없음'}</p>
        <hr style="border: none; border-top: 1px solid black; margin: 10px 0;">

        <p style="margin: 0;"><strong class="highlight">설명</strong></p>
        <p style="margin: 0;">${detail.overview || '설명 없음'}</p>
        <hr style="border: none; border-top: 2px solid black; margin: 10px 0;">

        <h5 style="margin-top: 10px; margin-bottom: 10px; background-color: #ffff99; display: inline;">시설 정보</h5>
        ${infoHtml}
    `;
}

// 즐겨찾기 상태 토글 함수
function setBookmarkToggleEvent() {
    const bookmarkIcons = document.querySelectorAll('.main-bookmark-icon');

    bookmarkIcons.forEach(bookmarkIcon => {
        // 고유 ID가 없으면 자동으로 생성
        let attractionId = bookmarkIcon.dataset.attractionId;
        let contentTypeId = bookmarkIcon.dataset.contentTypeId;
        let areaCode = bookmarkIcon.dataset.areaCode;

        if (!attractionId) {
            attractionId = getNextAttractionId();  // 고유 ID 생성
            bookmarkIcon.dataset.attractionId = attractionId;  // 아이콘에 고유 ID 저장
        }

        // 서버에서 즐겨찾기 상태 확인
        fetch(`/api/bookmarks/${attractionId}/status?contentTypeId=${contentTypeId}&areaCode=${areaCode}`)
            .then(response => response.json())
            .then(isBookmarked => {
                if (isBookmarked) {
                    bookmarkIcon.src = '/images/bookmark-icon.png';
                } else {
                    bookmarkIcon.src = '/images/bookmark-white_icon.png';
                }
            });

        // 클릭 이벤트 리스너
        bookmarkIcon.addEventListener('click', function () {
            const isBookmarked = bookmarkIcon.src.includes('bookmark-icon.png');  // 현재 북마크 상태 체크

            if (isBookmarked) {
                // 즐겨찾기 제거
                fetch(`/api/bookmarks/${attractionId}?contentTypeId=${contentTypeId}&areaCode=${areaCode}`, {
                     method: 'DELETE'
                })
                    .then(res => {
                        if (res.ok) {
                            bookmarkIcon.src = '/images/bookmark-white_icon.png';
                            console.log('즐겨찾기 제거됨');
                        }
                    });
            } else {
                // 즐겨찾기 추가
                fetch(`/api/bookmarks/${attractionId}?contentTypeId=${contentTypeId}&areaCode=${areaCode}`, {
                    method: 'POST'
                })
                    .then(res => {
                        if (res.ok) {
                            bookmarkIcon.src = '/images/bookmark-icon.png';
                            console.log('즐겨찾기 추가됨');
                        }
                    });
            }
        });
    });
}
