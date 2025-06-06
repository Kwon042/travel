document.addEventListener("DOMContentLoaded", function () {
    let currentUserId;

    // 로그인한 사용자 ID를 서버에서 가져오는 함수
    function fetchCurrentUserId() {
        fetch('/user/getCurrentUserId') // 현재 사용자 ID를 반환하는 API 엔드포인트
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    currentUserId = data.userId; // 사용자 ID 저장
                } else {
                    console.error("Failed to retrieve the user ID.");
                }
            })
            .catch(error => console.error("An error occurred:", error));
    }
    // 페이지 로드 시 사용자 ID 가져옴
    fetchCurrentUserId();

    // 🔹 프로필 이미지 모달 열기
    function openProfileImageModal() {
        const modal = document.getElementById('profileImageModal');
        const input = document.getElementById('profileImageInput');

        if (modal) {
            modal.style.display = 'flex'; // flex로 모달 표시
        }
    }

    // 🔹 프로필 이미지 모달 닫기
    function closeProfileImageModal() {
        const modal = document.getElementById('profileImageModal');
        const input = document.getElementById('profileImageInput');

        if (modal) {
            modal.style.display = 'none'; // 모달 숨기기
            input.value = ''; // 파일 입력 초기화
        } else {
            console.error("Unable to find the modal.");
        }
    }

    // 🔹 프로필 이미지 업로드
    window.uploadProfileImage = function() {
        const input = document.getElementById('profileImageInput');
        const file = input.files[0]; // 선택된 파일 가져오기

        if (!file) {
            alert("Choose the file.");
            return;
        }

        const formData = new FormData();
        formData.append('profileImage', file);
        formData.append('userId', currentUserId); // 사용자의 ID를 서버로 전송 (예시)

        // 서버로 파일 전송
        fetch('/api/image/user/uploadProfileImage', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || `서버에서 오류가 발생했습니다. 상태 코드: ${response.status}`);
                });
            }
            return response.json(); // JSON 응답 처리
        })
        .then(data => {
            console.log(data);

            if (data.success) {
                const profileImages = document.querySelectorAll('.profile-image');

                profileImages.forEach(img => {
                    img.src = data.newProfileImageUrl + "?t=" + new Date().getTime(); // 캐시 방지
                });

                alert("The profile image has been successfully uploaded.");
                closeProfileImageModal();
            } else {
                alert(data.message);
            }
        })
        .catch(error => {
            console.error("An error occurred during file upload: ", error);
            alert("An error occurred during file upload.");
        });
    }

    // 🔹 마이페이지에서 북마크한 관광지 목록 가져오기
    function loadBookmarks() {
        fetch(`/api/bookmarks/list`)
            .then(response => response.json())
            .then(bookmarks => {
                console.log("Bookmarked list:", bookmarks);

                const bookmarkContainer = document.getElementById('bookmarks');
                bookmarkContainer.innerHTML = '';

                if (bookmarks.length === 0) {
                    showEmptyMessage(bookmarkContainer);
                    return;
                }

                const grouped = groupBookmarksByType(bookmarks);
                renderGroupedBookmarks(grouped, bookmarkContainer);
            })
            .catch(error => {
                console.error("An error occurred while fetching the bookmarks:", error);
                alert("An error occurred while fetching the bookmarks.");
            });
    }

    function groupBookmarksByType(bookmarks) {
        const grouped = {};
        bookmarks.forEach(bookmark => {
            const typeId = bookmark.contentTypeId;
            if (!grouped[typeId]) {
                grouped[typeId] = [];
            }
            grouped[typeId].push(bookmark);
        });
        return grouped;
    }

    function renderGroupedBookmarks(grouped, container) {
        Object.keys(grouped).forEach(typeId => {
            const emoji = getEmojiByContentTypeId(typeId);
            const groupTitle = document.createElement('h5');
            groupTitle.classList.add('bookmark-group-title');
            groupTitle.textContent = `${emoji}   ${getTypeLabelById(typeId)}`;
            container.appendChild(groupTitle);

            grouped[typeId].forEach((bookmark, index) => {
                const bookmarkElement = createBookmarkElement(bookmark, emoji, index);
                container.appendChild(bookmarkElement);
            });
        });
    }

    function createBookmarkElement(bookmark, emoji, index) {
        const bookmarkElement = document.createElement('div');
        bookmarkElement.classList.add('bookmark-item');

        const displayIndex = index + 1;
        const imageUrl = bookmark.firstimage?.trim() ? bookmark.firstimage : '/images/no-image.png';

        bookmarkElement.innerHTML = `
            <span class="bookmark-id">${displayIndex}</span>
            <span class="bookmark-title">
                <span class="check">✔</span>${bookmark.title}
            </span>
            <img src="${imageUrl}" alt="이미지" class="bookmark-image">
            <button class="remove-button"
                    data-attraction-id="${bookmark.contentId}"
                    data-content-type-id="${bookmark.contentTypeId}"
                    data-area-code="${bookmark.areaCode}"
                    data-id="${bookmark.contentId}">Remove</button>
        `;

        bookmarkElement.querySelector('.bookmark-title').addEventListener('click', () => {
            showDetailModal(bookmark);
        });

        bookmarkElement.querySelector('.bookmark-image').addEventListener('click', () => {
            showDetailModal(bookmark);
        });

        bookmarkElement.querySelector('.remove-button').addEventListener('click', function () {
            const contentId = this.getAttribute('data-id');
            const contentTypeId = this.getAttribute('data-content-type-id');
            const areaCode = this.getAttribute('data-area-code');
            removeBookmark(contentId, contentTypeId, areaCode);
        });

        return bookmarkElement;
    }

    function getTypeLabelById(contentTypeId) {
        const labelMap = {
            "12": "관광지",
            "14": "문화시설",
            "15": "축제/공연",
            "28": "레포츠",
            "32": "숙박",
            "38": "쇼핑",
            "39": "음식점"
        };
        return labelMap[contentTypeId] || "기타";
    }

    function showEmptyMessage(container) {
        container.innerHTML = "<p>북마크된 관광지가 없습니다.</p>";
    }

    // 페이지 로드 시 북마크 목록 로드
    loadBookmarks();

    // 북마크 삭제 함수
    function removeBookmark(contentId, contentTypeId, areaCode) {
        // API 호출하여 북마크 삭제
        fetch(`/api/bookmarks/${contentId}?contentTypeId=${contentTypeId}&areaCode=${areaCode}`, {
            method: 'DELETE'
        })
        .then(res => {
            if (res.ok) {
                console.log('북마크가 성공적으로 제거되었습니다.');
                // 북마크가 삭제되면 목록을 새로 로드하여 UI 갱신
                loadBookmarks(); // UI 갱신을 위해 북마크 목록 다시 로드
            } else {
                console.error('북마크 제거 실패');
            }
        })
        .catch(error => {
            console.error('에러 발생:', error);
        });
    }

    // showDetailModal 함수 정의
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
                <h4 class="modal-title-text">${detail.title || '제목 없음'}</h4>
                <div class="image-group">
                    <img src="${detail.firstimage || '/images/no-image.png'}" alt="이미지" class="detail-image">
                </div>
            </div>
            <hr style="border: none; border-top: 2px solid black; margin: 10px 0;">
            <p style="margin: 0;"><strong class="highlight-detail">주소</strong></p>
            <p style="margin: 0;">${detail.addr1 || '정보 없음'}</p>
            <hr style="border: none; border-top: 1px solid black; margin: 10px 0;">

            <p style="margin: 0;"><strong class="highlight-detail">전화번호</strong></p>
            <p style="margin: 0;">${detail.tel || '정보 없음'}</p>
            <hr style="border: none; border-top: 1px solid black; margin: 10px 0;">

            <p style="margin: 0;"><strong class="highlight-detail">설명</strong></p>
            <p style="margin: 0;">${detail.overview || '설명 없음'}</p>
            <hr style="border: none; border-top: 2px solid black; margin: 10px 0;">

            <h5 style="margin-top: 10px; margin-bottom: 10px; background-color: #ffff99; display: inline;">시설 정보</h5>
            ${infoHtml}
        `;
    }

    // 🔹 비밀번호 수정 폼 보이기/숨기기
    function togglePasswordForm() {
        const form = document.getElementById('changePasswordForm');
        form.style.display = (form.style.display === "none") ? "block" : "none";
    }

    // 🔹 사용자 정보 수정 모달 열기
    function openEditModal(field) {
        let modalContent = `
            <span class="close" onclick="closeEditModal()">&times;</span> <!-- X 닫기 버튼 추가 -->
        `;

        switch (field) {
            case 'nickname':
                modalContent += `
                    <h4><span class="highlight">닉네임 수정</span></h4>
                    <input type="text" id="editNickname" placeholder="새 닉네임" required>
                    <button id="saveNickname">저장</button>
                `;
                break;
            case 'email':
                modalContent += `
                    <h4><span class="highlight">이메일 수정</span></h4>
                    <input type="email" id="editEmail" placeholder="새 이메일" required>
                    <button id="saveEmail">저장</button>
                `;
                break;
            default:
                return;
        }

        const modal = document.getElementById('editModal');
        const content = modal.querySelector('.modal-content');
        content.innerHTML = modalContent;
        modal.style.display = 'flex'; // 모달 열기

        // 저장 버튼 클릭 이벤트 추가 (한 번만 추가되도록 조치)
        setTimeout(() => {
            document.getElementById(`save${field.charAt(0).toUpperCase() + field.slice(1)}`)
                .addEventListener("click", () => saveEdit(field));
        }, 10);
    }

    // 🔹 정보 저장
    function saveEdit(field) {
        let inputValue;
        let updateField;

        if (field === 'nickname') {
            inputValue = document.getElementById('editNickname').value;
            updateField = 'nickname';
        } else if (field === 'email') {
            inputValue = document.getElementById('editEmail').value;
            updateField = 'email';
        } else {
            return;
        }

        // 🔹 서버로 닉네임(또는 이메일) 변경 요청 보내기
        fetch(`/user/mypage/update`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                field: updateField,
                value: inputValue
            })
        })
        .then(response => {
            if (!response.ok) {
                // 상태 코드가 200 ~ 299 범위가 아니면 오류 처리
                return response.json().then(data => {
                    throw new Error(data.message || `서버에서 오류가 발생했습니다. 상태 코드: ${response.status}`);
                });
            }
            return response.json(); // JSON 응답 처리
        })
        .then(data => {
            if (data.success) {
                // 서버에서 받은 메시지 출력
                alert(data.message);

                // 화면에 즉시 반영
                if (field === 'nickname') {
                    const currentNickname = document.getElementById('currentNickname');
                    if (currentNickname) {
                        currentNickname.innerText = inputValue;
                    }
                } else if (field === 'email') {
                    const currentEmail = document.getElementById('currentEmail');
                    if (currentEmail) {
                        currentEmail.innerText = inputValue;
                    }
                }
                closeEditModal(); // 모달 닫기
            } else {
                showErrorModal(data.message); // 오류 발생 시 메시지 모달 표시
            }
        })
        .catch(error => {
            console.error("An error occurred while updating: ", error.message);
            showErrorModal(error.message);
        });
    }

    // 🔹 모달 닫기
    function closeEditModal() {
        const modal = document.getElementById('editModal');
        if (modal) {
            modal.style.display = 'none'; // 모달 숨기기
        }
    }

    // 🔹 오류 모달 닫기
    function closeErrorModal() {
        const modal = document.getElementById('errorModal');
        if (modal) {
            modal.classList.add('hidden'); // 모달 숨기기
        }
    }

    // 모달 메세지 표시하는 함수
    function showErrorModal(message) {
        const errorMessage = document.getElementById('errorMessage');
        errorMessage.innerText = message; // 메시지 설정
        const modal = document.getElementById('errorModal');
        modal.classList.remove('hidden'); // 모달 열기
    }

    // URL 체크
    if (window.location.pathname === '/user/mypage/change_password') {
        modal.classList.remove('hidden'); // 모달 열기
        }

    // 비밀번호 수정 모달 열기
    window.openChangePasswordModal = function() {
        const modal = document.getElementById('changePasswordModal');
        if (modal) {
            modal.style.display = 'flex'; // 모달을 flex로 표시
        }
    }

    // 비밀번호 수정 모달 닫기
    window.closeChangePasswordModal = function() {
        const modal = document.getElementById('changePasswordModal');
        if (modal) {
            modal.style.display = 'none'; // 모달을 숨김
            document.getElementById('currentPassword').value = '';
            document.getElementById('newPassword').value = '';
            document.getElementById('newPasswordConfirm').value = '';
        }
    }

    function changePassword() {
        const currentPassword = document.getElementById('currentPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const newPasswordConfirm = document.getElementById('newPasswordConfirm').value;

        // 모든 필드 입력 확인
        if (!currentPassword || !newPassword || !newPasswordConfirm) {
            alert("모든 필드를 입력하세요.");
            return;
        }

        // 새 비밀번호와 확인 비밀번호가 동일한지 확인
        if (newPassword !== newPasswordConfirm) {
            alert("새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.");
            return;
        }

        // 새 비밀번호가 현재 비밀번호와 일치하지 않는지 확인
        if (newPassword === currentPassword) {
            alert("새 비밀번호는 현재 비밀번호와 동일할 수 없습니다.");
            return;
        }

        // 비밀번호 변경 요청
        fetch('/user/mypage/change_password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `currentPassword=${encodeURIComponent(currentPassword)}&newPassword=${encodeURIComponent(newPassword)}`
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || `서버에서 오류가 발생했습니다. 상태 코드: ${response.status}`);
                });
            }
            return response.text();
        })
        .then(data => {
            alert("Your password has been successfully changed.");
            closeChangePasswordModal(); // 모달 닫기
        })
        .catch(error => {
            console.error("Error during password change: ", error);
            alert("An error occurred while changing the password.");
        });
    }

    // 회원 탈퇴 버튼 클릭 이벤트
    const deleteButton = document.getElementById('deleteAccountButton');
    console.log(deleteButton);
    if (deleteButton) {
        deleteButton.addEventListener('click', function () {
            alert("Button clicked!");
            if (confirm("Are you sure you want to delete your account?")) {
                fetch('/user/deleteAccount', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                })
                .then(response => {
                    if (!response.ok) {
                        throw new Error("Account deletion failed.");
                    }
                    return response.json();
                })
                .then(data => {
                    alert(data.message);
                    window.location.href = "/";
                })
                .catch(error => {
                    console.error("Error during account deletion: ", error);
                    alert("An error occurred while deleting your account.");
                });
            }
        });
    }

    // 전역 등록
    window.openProfileImageModal = openProfileImageModal;
    window.closeProfileImageModal = closeProfileImageModal;
    window.openEditModal = openEditModal;
    window.togglePasswordForm = togglePasswordForm;
    window.closeEditModal = closeEditModal;
    window.closeErrorModal = closeErrorModal;
    window.showErrorModal = showErrorModal;
    window.changePassword = changePassword;
    window.loadBookmarks = loadBookmarks;
});
