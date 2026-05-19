# SnapMind

[cite_start]스크린샷과 사진을 저장한 '목적'과 '맥락'을 기록하는 AI 기반 안드로이드 메모 앱입니다. [cite: 1, 3] [cite_start]TensorFlow Lite로 이미지를 분류하고 ML Kit(OCR)로 텍스트를 추출합니다. [cite: 5, 7] [cite_start]OpenAI API로 태그를 자동 생성해 단순한 이미지 보관을 넘어 언제든 쉽게 검색할 수 있는 스마트 지식 베이스를 제공합니다. [cite: 8, 11]

## 💡 프로젝트 배경 및 목적
[cite_start]사용자는 유튜브, 강의 자료, 코드 에러, 쇼핑 아이템 등 다양한 정보를 기억하기 위해 스크린샷을 자주 캡처합니다. [cite: 2] [cite_start]하지만 시간이 지나면 "어디에 쓰려고 했는지", "왜 저장했는지" 기억하지 못하는 문제가 발생합니다. [cite: 2] [cite_start]이 프로젝트는 AI를 활용해 이미지의 텍스트와 맥락을 자동 추출하고, 사용자가 저장 목적을 간편하게 기록할 수 있도록 돕기 위해 기획되었습니다. [cite: 1, 3]

## ✨ 주요 기능
* [cite_start]**스마트 공유 및 캡처 연동:** 다른 앱 사용 중에도 안드로이드 기본 `ACTION_SEND` Intent를 통해 앱 전환 없이 이미지를 즉시 전달하고 저장할 수 있습니다. [cite: 4, 5]
* [cite_start]**온디바이스 AI 이미지 분류:** TensorFlow Lite를 활용해 자체 학습된 CNN 모델이 이미지를 비디오, 코드, 쇼핑, 텍스트, 지도, 밈 등의 카테고리로 자동 분류합니다. [cite: 5, 17, 18]
* [cite_start]**OCR 텍스트 추출:** Google ML Kit Text Recognition을 사용하여 이미지 내의 핵심 텍스트와 에러 로그 등을 자동으로 추출합니다. [cite: 7]
* [cite_start]**AI 태그 자동 추천:** 추출된 OCR 텍스트와 이미지 분류 결과를 바탕으로 OpenAI API가 적절한 태그(예: #버그, #과제, #구매예정)를 분석하고 추천합니다. [cite: 8]
* [cite_start]**목적 중심 메모:** 사용자가 나중에 참고할 내용이나 저장한 구체적인 이유를 간단하게 기록할 수 있습니다. [cite: 9, 10]
* [cite_start]**강력한 검색 및 필터링:** Room DB와 SearchView를 기반으로 태그, 카테고리, 날짜 등을 필터링하여 원하는 맥락의 이미지를 빠르게 찾을 수 있습니다. [cite: 10, 11, 12]

## 🛠 기술 스택
| 구분 | 기술 및 라이브러리 |
| --- | --- |
| **Language** | [cite_start]Kotlin [cite: 13] |
| **Architecture & UI** | [cite_start]Android Jetpack (ViewModel, Navigation Component, Fragment), RecyclerView [cite: 13, 14] |
| **Asynchronous** | [cite_start]Kotlin Coroutines [cite: 13, 15] |
| **Network & Media** | [cite_start]Retrofit (API 통신), Glide (이미지 로딩) [cite: 13, 15] |
| **Local DB** | [cite_start]Room Database [cite: 12, 13] |
| **AI / ML** | [cite_start]TensorFlow Lite (이미지 분류), ML Kit (OCR 텍스트 추출) [cite: 5, 7, 13] |
| **External API** | [cite_start]OpenAI API, YouTube API (선택), Notion API (선택) [cite: 15, 16] |

## 📂 앱 구조
* [cite_start]**`HomeActivity`:** 최근 저장된 스크린샷 목록과 카테고리별 분류 화면을 제공합니다. [cite: 12]
* [cite_start]**`AddScreenshotActivity`:** 공유 Intent를 통해 이미지를 수신하고, 비동기로 OCR 및 ML 분류와 태그 추출 작업을 수행합니다. [cite: 12, 15]
* [cite_start]**`DetailActivity`:** 저장된 이미지의 상세 정보를 확인하고, 메모 내용과 태그를 수정할 수 있습니다. [cite: 12]
* [cite_start]**`SearchActivity`:** 저장된 기록들을 태그, 제목, 카테고리 기반으로 검색하고 필터링합니다. [cite: 6, 12]
