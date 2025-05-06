// 카카오 맵을 초기화하는 코드
let map;
let markers = [];
let activeInfoWindow = null;

window.onload = function() {
    // 카카오맵이 정상적으로 로드되었는지 확인
    if (typeof kakao === 'undefined') {
        alert("카카오맵 API 로딩 실패");
        return;
    }

    // 카카오 맵을 초기화하는 코드
    map = new kakao.maps.Map(document.getElementById('map'), {
        center: new kakao.maps.LatLng(37.5665, 126.9780), // 서울 중심
        level: 7
    });

    // 카테고리 버튼 클릭 이벤트 (선택된 지역 + contentTypeId로 검색)
    document.querySelectorAll('.category-btn').forEach(button => {
        button.addEventListener('click', (event) => {
            const regionName = document.getElementById('searchKeyword').value.trim();
            if (!regionName) {
                alert("먼저 지역명을 입력해주세요.");
                return;
            }

            // 버튼 시각적 표시
            document.querySelectorAll('.category-btn').forEach(btn => btn.classList.remove('selected'));

            const clickedButton = event.currentTarget;
            clickedButton.classList.add('selected');

            const contentTypeId = clickedButton.getAttribute('data-type');
            console.log('contentTypeId:', contentTypeId);  // 이 로그를 추가하여 확인

            searchAttraction(regionName, contentTypeId);
        });
    });
};

function searchAttraction(regionName, contentTypeId) {
    showLoading(); // 로딩 표시
    const url = `/api/attraction/search?regionName=${encodeURIComponent(regionName)}&contentTypeId=${contentTypeId}`;

    fetch(url)
        .then(response => response.json())
        .then(data => {
            hideLoading();
            console.log("검색 결과:", data);

            if (Array.isArray(data) && data.length > 0) {
                displayMarkersOnMap(data);
            } else {
                alert("검색된 관광지가 없습니다.");
            }
        })
        .catch(error => {
            hideLoading(); // 로딩 표시
            console.error('검색 오류:', error);
            alert('검색 도중 오류가 발생했습니다.');
        });
}

function loadAttractions() {
    fetch('/api/attractions')
        .then(response => response.json())
        .then(data => {
            if (Array.isArray(data) && data.length > 0) {
                displayMarkersOnMap(data);
            } else {
                console.warn('데이터 없음 또는 잘못된 형식');
            }
        })
        .catch(error => {
            console.error('초기 관광지 로드 실패:', error);
            alert('잘못된 데이터 형식입니다.');
        });
}

function fetchAndShowDetail(contentId, fallbackImage, contentTypeId, areaCode) {
    console.log('상세보기 요청 - contentId:', contentId, 'contentTypeId:', contentTypeId, 'areaCode:', areaCode);

    fetch(`/api/attraction/detail/${contentId}/${contentTypeId}?areaCode=${areaCode}`)
        .then(res => res.json())
        .then(detail => {
            detail.contentTypeId = contentTypeId;
            console.log(detail); // 디버깅을 위해 로그 추가
            if (!detail.firstimage || detail.firstimage === '') {
                detail.firstimage = fallbackImage;
            }
            showDetailModal(detail);
        })
        .catch(error => {
            console.error('상세정보 로딩 실패:', error);
            alert('상세정보를 불러오는 데 실패했습니다.');
        });
}

function displayMarkersOnMap(data) {
    // 기존 마커 제거
    markers.forEach(marker => marker.setMap(null));
    markers = [];

    if (data.length > 0) {
        // 첫 번째 마커 위치로 지도 이동
        map.setCenter(new kakao.maps.LatLng(data[0].mapy, data[0].mapx));
    }

    data.forEach(function(item) {

        const markerPosition = new kakao.maps.LatLng(item.mapy, item.mapx);
        const marker = new kakao.maps.Marker({
            position: markerPosition,
            title: item.title
        });
        marker.setMap(map);
        markers.push(marker);

        const imageUrl = item.firstimage?.trim() ? item.firstimage : '/images/no-image.png';

        // 인포윈도우 내용
        const infowindowContent = `
            <div class="kakao-map-info-window-content">
                <strong>${item.title}</strong>
                <img src="${imageUrl}" alt="${item.title}">
                <button onclick="fetchAndShowDetail(${item.contentId}, '${imageUrl}', ${item.contentTypeId}, ${item.areaCode})">상세보기</button>
            </div>
        `;

        const infowindow = new kakao.maps.InfoWindow({
            content: infowindowContent
        });

        kakao.maps.event.addListener(marker, 'click', function() {
            if (activeInfoWindow) {
                activeInfoWindow.close();
            }
            infowindow.open(map, marker);
            activeInfoWindow = infowindow;
        });
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
        <div class="modal-header-container">
            <h4 class="modal-title-text">${emoji} ${detail.title || '제목 없음'}</h4>
            <div class="bookmark-image-group">
                <img id="bookmarkIcon" class="bookmark-icon"
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
        <p style="margin: 0;"><strong>주소</strong></p>
        <p style="margin: 0;">${detail.addr1 || '정보 없음'}</p>
        <hr style="border: none; border-top: 1px solid black; margin: 10px 0;">

        <p style="margin: 0;"><strong>전화번호</strong></p>
        <p style="margin: 0;">${detail.tel || '정보 없음'}</p>
        <hr style="border: none; border-top: 1px solid black; margin: 10px 0;">

        <p style="margin: 0;"><strong>설명</strong></p>
        <p style="margin: 0;">${detail.overview || '설명 없음'}</p>
        <hr style="border: none; border-top: 2px solid black; margin: 10px 0;">

        <h5 style="margin-top: 10px; margin-bottom: 10px;">시설 정보</h5>
        ${infoHtml}
    `;
}

let currentAttractionId = 1; // 클라이언트에서 관리하는 ID (시작 값은 1)

function getNextAttractionId() {
    return currentAttractionId++;  // ID 증가시키고 반환
}

// 즐겨찾기 상태 토글 함수
function setBookmarkToggleEvent() {
    const bookmarkIcons = document.querySelectorAll('.bookmark-icon');

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

// 상세보기 페이지 이동
function moveToDetail(contentId) {
    window.location.href = `/attraction/detail/${contentId}`;
}

function showLoading() {
    document.getElementById("loadingSpinner").style.display = "block";
}

function hideLoading() {
    document.getElementById("loadingSpinner").style.display = "none";
}
