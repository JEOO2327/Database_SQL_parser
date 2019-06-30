# test-JEOO2327

PRND back-end test
- Language : Python (Django)
- DB : SQLite

-----------------------------------------------------------------------
- 데이터 베이스 생성, 브랜드, 차종, 모델 데이터 import, 차량 dummy 데이터 생성
  - git clone https://github.com/PRNDcompany/test-JEOO2327.git
  - cd test-JEOO2327
  - pip3 install -r requirements.txt
  - cd PRNDtest
  - python3 manage.py makemigrations
  - python3 manage.py migrate
  - python3 init_manufacturer.py
    - 기존의 브랜드, 차종, 모델명 객체를 제거하고 ~/data/init.xlsx 파일을 읽어서 새로운 브랜드, 차종, 모델명 객체를 생성한다.
  - python3 create_vehicles.py
    - 반복횟수를 입력하는 창이 나온다. 한번 반복할 때 마다 랜덤하게 차량 객체 140개를 생성한다. 승인여부 및 경매 시작 시간도 랜덤이다. \
    각 차량 모델마다 5~7개의 사진 객체를 생성해준다. 차량 객체들이 외래키로 가질 test 유저들도 생성이 된다.
    - 모델이 많기 때문에 이것을 여러번 실행시켜서 데이터를 많이 만들어야 count와 filter의 결과를 테스트하기 편할 것이다. (이미지 파일을 업로드하기 때문에 많이 실행하면 조금 오래 걸릴 수 있다.)
- admin 유저 생성
  - python3 manage.py createsuperuser
- 실행
  - python3 manage.py runserver

-------------------------------------------------------------------
- 위의 방법으로 실행하였다면 기본 url은 http://127.0.0.1:8000 이며 아래에 나오는 API를 url에 추가하여 api를 이용할 수 있다. \
ex) 회원가입 url : http://127.0.0.1:8000/user/accounts/register/
- api를 테스트할 때 대부분의 url은 브라우저를 통해서도 이용할 수 있지만, 차량 등록 api, 차량 수정 api의 경우 브라우저를 통해 image를 upload하기 힘들기 때문에 postman을 사용하는 것을 추천한다.
- postman을 사용할 경우 Authorization을 이용하여 인증을 하거나 혹은, 로그인 API에 POST 요청을 보내서 sessionid를 얻고 request를 보낼 때 sessionid를 이용하여 인증을 할 수 있다. 다른 계정으로 인증을 하고 싶다면 Cookies에서 sessionid를 제거하여야 한다.
------------------------------------------------------------------------
- User (회원가입, 로그인, 로그아웃)
  - 회원가입: username, password, password confirm을 입력하여 가입한다. (POST 요청)
    - API: http:///user/accounts/register/
    - permission: Allow any
    - json body: {\
        "username": "testuser", \
        "password": "testpassword!@#$", \
        "password_confirm": "testpassword!@#$" \
    }
  - 로그인: username, password를 입력하여 로그인한다. (POST 요청)
    - API: http:///user/accounts/login/
    - permission: Allow any
    - json body: {\
        "login": "testuser",\
        "password": "testpassword!@#$"\
    }
  - 로그아웃 (POST 요청) (로그아웃은 Postman으로 테스트 불가능, 브라우저로 테스트 할 경우 이용 가능)
    - API: http:///user/accounts/logout/
    - permission: Allow any
    - json body: {}
    
------------------------------------------------------------
- Manufacturer (차량 검색 필터 목록) API
  - 차량 검색 필터 목록: 브랜드, 차종, 모델 각 필터에 차량이 얼마나 존재하는지 count (승인된 차량의 count, 리스트의 형태로 나타냄, count가 1 이상인 것만 표시) \
  결과에는 filter_url과 count_url이 포함되어 있으며, filter_url은 해당 브랜드, 차종, 모델로 filtering한 결과 url이고, count_url은 해당 브랜드에 포함된 차종들의 count 결과 url, 해당 차종에 포함된 모델들의 count 결과 url이다.
    - 브랜드 검색 필터 목록: 브랜드 목록과 각 브랜드 필터에 존재하는 차량 count (GET 요청)
      - API: http:///manufacturer/count/brand/
      - permission: Authenticated (로그인 필요)
    - 차종 검색 필터 목록: 특정 브랜드에 포함된 차종 목록과 각 차종 필터에 존재하는 차량 count (GET 요청)
      - API: http:///manufacturer/count/brand/:brand_id/category/ \
      :brand_id는 brand의 primary key이며 양의 정수이다.
      - permission : Authenticated (로그인 필요)
    - 모델 검색 필터 목록: 특정 브랜드, 차종에 포함된 모델 목록과 각 모델 필터에 존재하는 차량 count (GET 요청)
      - API: http:///manufacturer/count/brand/:brand_id/category/:category_id/model/ \
      :category_id는 category의 primary key이며 양의 정수이다.
      - permission: Authenticated (로그인 필요)
------------------------------------------------------------------
- vehicle (차량 등록, 경매 승인, 차량 목록, 검색된 차량 목록, 차량 상세)
  - 차량 등록 (POST): 차량 정보와 사진을 입력하여 차량 생성 (POST 요청)
    - API: http:///vehicle/post/
    - permission : Authenticated (로그인 필요)
    - Body: ![VehiclePostBody](./ReadmeImage/VehiclePostBody.PNG)
      - 이미지 파일의 경우 images 에 넣어주어야 한다.
      - 브랜드, 차종, 모델명은 각각 brand, category, model field에 정확한 이름을 입력해주어야 한다.
      - 변속기는 auto_transmission field에 자동일 경우 True, 수동일 경우 False를 입력해주어야 한다. 
      - 연료는 fuel field에 lpg, gasoline, diesel, hybrid, electric, bifuel 중 하나를 입력해주어야 한다.
      - 주행거리는 양의 정수를 입력해주어야 한다.
  - 경매 승인
    - 승인 대기 중인 차량 목록 (GET)
      - API: http:///vehicle/approve-waiting/list/
      - permission: Admin
    - 경매 승인/취소 : 특정 차량의 경매 승인 여부를 확인하고, 변경한다. (GET, PUT 요청)  
      - API: http:///vehicle/approve/:vehicle_id
      - permission: Admin
      - 승인: 승인되지 않은 차량의 경우 승인 요청을 보내면 현재 시간이 경매 시작시간으로 등록되고, is_approved가 True로 변한다. (PUT 요청) 
        - json body: {\
                      "is_approved": true \
                      } 
      - 승인 취소: 승인된 차량의 경우 취소 요청을 보내면 경매 시작시간이 None으로 변하고, is_approved가 False로 변한다. (PUT 요청)
        - json body: {\
                      "is_approved": false\
                      } 
  - 승인된 차량 목록: 경매 진행 중이거나 종료된 모든 차량 목록을 가져온다. (GET 요청)
    - API: http:///vehicle/approved/order/:order \
                      
  - 검색된 차량 목록: 경매 진행 중이거나 종료된 차량들 중에서 브랜드, 차종, 모델명으로 필터링 된 차량 목록을 가져온다. (GET 요청)
      :order는 latest 와 old 중 하나를 선택할 수 있으며 각각 최신 순 오래된 순으로 정렬한다.
      - permission: Authenticated (로그인 필요)
    - 브랜드 필터링: 특정 브랜드에 속하는 차량들 목록을 가져온다. 
      - API: http:///vehicle/filter/brand/:brand_id/order/:order \
      :brand_id는 brand의 primary key 이며 양의 정수이다. 
      - permission: Authenticated (로그인 필요)
    - 차종 필터링: 특정 브랜드의 특정 차종에 속하는 차량들 목록을 가져온다.
      - API: http:///vehicle/filter/brand/:brand_id/category/:category_id/order/:order \
      :category_id는 category의 primary key 이며 양의 정수이다. 
      - permission: Authenticated (로그인 필요)
    - 모델 필터링: 특정 브랜드의 특정 차종의 특정 모델에 속하는 차량들 목록을 가져온다.
      - API: http:///vehicle/filter/brand/:brand_id/category/:category_id/model/:model_id/order/:order \
      :model_id는 model의 primary key이며 양의 정수이다.
      - permission: Authenticated (로그인 필요)
    
  - 차량 상세 : 특정 차량의 상세 정보를 가져오거나, 삭제하거나, 수정한다. (수정 시 사진을 추가할 수 있음, 사진 변경 및 삭제는 다른 API를 통해 가능) (GET, DELETE, PUT 요청)
    - API: http:///vehicle/detail/:vehicle_id
    - permission: IsOwnerOrReadOnly (본인이 등록한 차량이 아니면 GET만 가능, staff는 모든 요청 가능), Authenticated (로그인 필요)
    - Body (PUT):
      - 차량 정보 수정 (사진 추가 X)
      ![UpdateVehicleNoPhoto](./ReadmeImage/UpdateVehicleNoPhoto.PNG)
      - 차량 정보 수정 (사진 추가 O)
      ![UpdateVehicleAddPhoto](./ReadmeImage/UpdateVehicleAddPhoto.PNG)
  - 차량 이미지 수정 : 특정 차량에 등록된 이미지를 수정하거나 삭제한다. (GET, DELETE, PUT 요청)    
    - API: http:///vehicle/detail/:vehicle_id/image/:image_index \
    :image_index는 사진의 번호이며 0부터 시작한다.
    - permission: IsOwnerOrReadOnly, Authenticated
    - Body (PUT):
    ![UpdateVehicleImage](./ReadmeImage/UpdateVehicleImage.PNG)
    
----------------------------
- 추가 기능
  - 유저 전체 리스트 받아오기 (GET)
    - API: http:///user/list/
    - permission: Admin

  - 브랜드, 차종, 모델 - 생성 및 전체 리스트 (POST, GET)
    - 브랜드 생성: 브랜드 이름을 입력하여 브랜드를 생성한다. / 브랜드 전체 리스트를 가져온다. (POST, GET 요청)  
      - API: http:///manufacturer/brand/post/
      - permission: Admin
      - json body: {\
          "brand_name": "testbrand"\
      }
    - 차종 생성: 차종 이름, brand id를 입력하여 차종을 생성한다. / 차종 전체 리스트를 가져온다. (POST, GET 요청)
      - API: http:///manufacturer/category/post/
      - permission: Admin
      - json body: {\
          "category_name": "testcategory",\
          "brand": 69\
      }
    - 모델 생성 : 모델 이름, category id, start_year, end_year를 입력하여 모델을 생성한다. / 모델 전체 리스트를 가져온다. (POST, GET 요청)
      - API: http:///manufacturer/model/post/
      - permission : Admin
      - json body : {\
          "model_name": "testmodel",\
          "category": 866,\
          "start_year": 2015,\
          "end_year": 2017\
      }  
  - 브랜드, 차종, 모델 상세 정보 보기, 수정, 삭제 (GET, PUT, DELETE)
    - 브랜드 상세 정보 (GET, PUT, DELETE 요청)
      - API: http:///manufacturer/brand/detail/:brand_id
      - permission : Admin
      - json body : {\
          "brand_name": "수정된_브랜드_이름"\
      }
    - 차종 상세 정보 (GET, PUT, DELETE 요청)
      - API: http:///manufacturer/category/detail/:category_id
      - permission : Admin
      - json body : {\
          "category_name": "수정된_차종_이름",\
          "brand": 1\
      }
    - 모델 상세 정보 (GET, PUT, DELETE 요청)
      - API: http:///manufacturer/model/detail/:model_id
      - permission : Admin
      - json body : {\
         "model_name": "수정된_모델_이름",\
          "start_year": 2017,\
          "end_year": 2019,\
          "category": 19\
      }
  - 전체 차량 보기 (승인되지 않은 차량도 볼 수 있음) (GET 요청)
    - API: http:///vehicle/list/
    - permission : Admin
