document.addEventListener('DOMContentLoaded', function() {
    // 모든 'attraction-card' 클래스 요소를 클릭 가능한 요소로 설정
    const attractionCards = document.querySelectorAll('.attraction-card');

    attractionCards.forEach(card => {
        card.addEventListener('click', function() {
            const contentId = card.getAttribute('data-content-id');
            const contentTypeId = card.getAttribute('data-content-type-id');
            const areaCode = card.getAttribute('data-area-code');

            // 콘솔에 출력 (디버깅용)
            console.log(`Clicked Attraction: ${contentId}, ${contentTypeId}, ${areaCode}`);
            // 세부 정보를 보여주는 페이지로 이동
            window.location.href = `/attractionDetail/${contentId}?contentTypeId=${contentTypeId}&areaCode=${areaCode}`;
        });
    });
});
