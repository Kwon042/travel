// 카카오 맵을 초기화하는 코드
let map;

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
            console.log(result);  // 응답 확인을 위한 로그 추가
            const items = result?.response?.body?.items?.item;

            if (!items || items.length === 0) {
                console.error('검색된 관광지가 없습니다.');
                alert('검색된 관광지가 없습니다.');
                return;
            }
            displayMarkersOnMap(items);
        })
        .catch(error => {
            console.error('검색 오류:', error);
            alert('검색 도중 오류가 발생했습니다.');
        });
}

let markers = [];  // 마커 배열을 전역으로 선언

// 검색된 관광지 리스트를 카카오 지도에 마커로 표시
function displayMarkersOnMap(data) {
    // 기존 마커 제거
    markers.forEach(marker => marker.setMap(null));
    markers = [];

    data.forEach(function(item) {
        const markerPosition = new kakao.maps.LatLng(item.mapy, item.mapx);
        const marker = new kakao.maps.Marker({
            position: markerPosition,
            title: item.title
        });
        marker.setMap(map);
        markers.push(marker);
    });
}