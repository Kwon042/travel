document.addEventListener('DOMContentLoaded', () => {
    const profileImages = document.querySelectorAll('.profile-image');
    const profileDropdown = document.getElementById('profileDropdown');

    // profileDropdown이 존재하는지 확인
    if (profileDropdown) {
        // 각 프로필 이미지에 대해 클릭 이벤트 처리
        profileImages.forEach(profileImage => {
            profileImage.addEventListener('click', (event) => {
                event.stopPropagation();  // 다른 곳 클릭 시 드롭다운 닫히지 않도록 막기
                profileDropdown.style.display = (profileDropdown.style.display === 'block') ? 'none' : 'block';
            });
        });

        // 다른 곳 클릭하면 드롭다운 닫히기
        window.addEventListener('click', (event) => {
            if (!event.target.closest('.profile-menu')) {
                profileDropdown.style.display = 'none';
            }
        });
    }
});
