1. 게시글 이미지
2. 게시판 목록 댓글, 좋아요
3. 게시글 댓글

db의
id 1:
url>> /uploads/profile/1/images-2.jpeg 이렇게 저장됨.
웹에서도 URL: http://localhost:8081/uploads/profile/1/images-2.jpeg 이렇게 요청됨.
id 2:
url>> /uploads/profile/1/a3b4f79c-2d07-4863-8c0e-4182ae1a8852_images-2.jpeg 이렇게 저장됨. >> 디렉토리에도 /uploads/profile/1 에서도 이 파일명으로 저장됨.
그러니까 404 오류가 나지.


1. ImageService
- 이미지 업로드, 저장, 삭제 등을 처리 (여기서 처리하는 이미지 파일은 프로필 이미지 & 리뷰 이미지)
- 파일 크기와 형식도 검증하여 유효한 이미지 파일만 저장하도록
2. ReviewBoardService
- 리뷰 게시판의 생성, 수정 및 조회를 관리 (리뷰 게시판에는 이미지도 첨부 가능)
- 이미지 업로드가 필요할 때, ImageService를 통해 이미지를 저장하고, 이미지 url을 리뷰 게시판에 추가
3. UserService
- 사용자 관련 작업을 처리 (사용자의 닉네임, 이메일, 프로필 이미지 등을 수정할 수 있다.)
- 프로필 이미지 업로드 시 ImageService를 이용하여 이미지를 저장하고, 저장된 이미지 url을 사용자 정보에 추가
4. ReviewBoardController
- HTTP 요청을 처리하느 컨트롤러 (리뷰 게시판 페이지를 보여주거나, 게시글을 생성 및 수정하는 작업을 처리)
- ReviewBoardService와 상호작용하여 게시판의 비즈니스 로직을 처리


1. findByRegion(Region region)
- 이 메서드는 ReviewBoard 엔티티에서 region 필드(즉, Region 객체)와 직접 비교
- ReviewBoard에 저장된 Region 객체와 region 파라미터로 전달된 Region 객체를 비교하여, 해당 Region과 일치하는 ReviewBoard 목록을 반환
- 이 메서드는 ReviewBoard가 Region 객체와 일치하는지를 비교합니다. Region 객체는 id, regionName 등의 필드를 가질 수 있으며, 객체 간의 비교가 이루어진다.
> Region 객체 전체와 비교하는 메서드 / region 객체의 모든 필드가 일치하는지 확인
2. findByRegion_RegionName(String regionName)
- 이 메서드는 ReviewBoard의 region 객체에 포함된 regionName 속성 값과 regionName 파라미터를 비교
- ReviewBoard의 Region 객체 내의 regionName 속성과 주어진 문자열 regionName을 비교하여 일치하는 ReviewBoard 목록을 반환
- 이 메서드는 Region 객체 내의 regionName 필드를 기준으로 필터링
- regionName은 문자열이므로 Region 객체의 속성 중 하나에 대해서만 비교가 이루어진다.
> Region 객체의 특정 필드인 regionName만을 기준으로 비교


3. CSRF 토큰이 포함되어야 하는 요청

- POST 요청: 사용자가 데이터를 생성할 때 (예: 글 작성 시)
- PUT 요청: 사용자가 데이터를 수정할 때 (예: 글 수정 시)
- DELETE 요청: 사용자가 데이터를 삭제할 때 (예: 글 삭제 시)