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

    // 검색창에서 엔터 눌렀을 때 검색 실행
    const searchInput = document.getElementById('searchKeyword');
    searchInput.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') {
            searchAttraction();
        }
    });
};

// 검색 요청을 보낼 함수
function searchAttraction() {
    const keyword = document.getElementById('searchKeyword').value.trim();
    if (!keyword) {
        alert("검색어를 입력해주세요.");
        return;
    }

    fetch(`/api/attraction/search?keyword=${encodeURIComponent(keyword)}`)
        .then(response => response.json())
        .then(result => {
            console.log(result);

            // 결과가 빈 배열이 아니라면 마커 표시
            if (result && Array.isArray(result) && result.length > 0) {
                displayMarkersOnMap(result);
            } else {
                console.error('검색된 관광지가 없습니다.');
                alert('검색된 관광지가 없습니다.');
            }
        })
        .catch(error => {
            console.error('검색 오류:', error);
            alert('검색 도중 오류가 발생했습니다.');
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

        // 인포윈도우 내용: 제목 + 이미지 + 좋아요 + 상세보기
        const infowindowContent = `
            <div style="padding:10px; text-align:center;">
                <strong>${item.title}</strong><br>
                <img src="${item.firstimage}" alt="${item.title}" style="width:100px;height:100px;object-fit:cover;"><br>
                <button onclick="likeAttraction(${item.contentId})" style="margin-top:5px;">❤️ 좋아요</button><br>
                <button onclick="moveToDetail(${item.contentId})" style="margin-top:5px;">상세보기</button>
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