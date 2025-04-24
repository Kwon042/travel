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

            // 버튼 클릭 또는 내부 요소(span 등) 클릭 시에도 항상 button에 selected 부여
            const clickedButton = event.currentTarget;
            clickedButton.classList.add('selected');
            event.target.classList.add('selected');

            const contentTypeId = event.target.getAttribute('data-type');
            searchAttraction(regionName, contentTypeId);
        });
    });
};

function searchAttraction(regionName, contentTypeId) {
    fetch(`/api/attraction/search?regionName=${encodeURIComponent(regionName)}&contentTypeId=${contentTypeId}`)
        .then(response => response.json())
        .then(data => {
            console.log("검색 결과:", data);

            if (Array.isArray(data) && data.length > 0) {
                displayMarkersOnMap(data);
            } else {
                alert("검색된 관광지가 없습니다.");
            }
        })
        .catch(error => {
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

        const imageUrl = item.firstImage && item.firstImage !== 'undefined' ? item.firstImage : 'images/no-image.png';

        // 인포윈도우 내용
        const infowindowContent = `
            <div class="kakao-map-info-window-content">
                <strong>${item.title}</strong>
                <img src="${imageUrl}" alt="${item.title}">
                <button onclick="fetchAndShowDetail(${item.contentId})">상세보기</button>
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

function fetchAndShowDetail(contentId) {
    fetch(`/api/attraction/detail/${contentId}`)
        .then(res => res.json())
        .then(detail => {
            console.log(detail); // 디버깅을 위해 로그 추가
            showDetailModal(detail);
        })
        .catch(error => {
            console.error('상세정보 로딩 실패:', error);
            alert('상세정보를 불러오는 데 실패했습니다.');
        });
}

function showDetailModal(detail) {
    const modalBody = document.getElementById('modalBody');
    let infoHtml = '';

    // infoList가 존재하는 경우에만 반복문 실행
    if (Array.isArray(detail.infoList) && detail.infoList.length > 0) {
        detail.infoList.forEach(info => {
            infoHtml += `
                <div>
                    <strong>${info.infoName || '정보 없음'}</strong>
                    <p>${info.infoText || '정보 없음'}</p>
                </div>
            `;
        });
    } else {
        infoHtml = '<p>추가 정보가 없습니다.</p>';
    }

    // 모달의 내용 설정
    modalBody.innerHTML = `
        <h4>${detail.title || '제목 없음'}</h4>
        <p><strong>주소:</strong> ${detail.addr || '정보 없음'}</p>
        <p><strong>전화번호:</strong> ${detail.tel || '정보 없음'}</p>
        <p><strong>설명:</strong> ${detail.description || '설명 없음'}</p>
        <img src="${detail.firstimage || '/images/no-image.png'}"
             alt="이미지" style="width:200px; height:150px; object-fit:cover;">

        <h5>시설 정보</h5>
        ${infoHtml}
    `;

    // 부트스트랩 모달을 사용하여 모달 띄우기
    const modal = new bootstrap.Modal(document.getElementById('attractionModal'));
    modal.show();
}

// 좋아요 처리 함수 (API 호출 방식으로 구현 가능)
function likeAttraction(contentId) {
    fetch(`/api/attraction/like/${contentId}`, {
        method: 'POST'
    })
    .then(response => {
        if (response.ok) {
            alert('좋아요를 눌렀습니다!');
        } else {
            alert('이미 좋아요를 누른 관광지입니다.');
        }
    })
    .catch(error => {
        console.error('좋아요 실패:', error);
        alert('좋아요 처리 중 오류가 발생했습니다.');
    });
}

// 상세보기 페이지 이동
function moveToDetail(contentId) {
    window.location.href = `/attraction/detail/${contentId}`;
}