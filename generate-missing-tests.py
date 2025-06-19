#!/usr/bin/env python3
"""
AI 기반 누락된 테스트 자동 감지 및 생성 스크립트
"""

import os
import re
import ast
from typing import List, Dict, Set


class TestGapAnalyzer:
    def __init__(self, project_root: str):
        self.project_root = project_root
        self.main_sources = []
        self.test_sources = []
        self.missing_tests = []

    def scan_project(self):
        """프로젝트 전체를 스캔해서 소스와 테스트 파일들을 찾습니다."""
        for root, dirs, files in os.walk(self.project_root):
            for file in files:
                if file.endswith(".kt"):
                    file_path = os.path.join(root, file)
                    if "test" in root or "androidTest" in root:
                        self.test_sources.append(file_path)
                    elif "main" in root:
                        self.main_sources.append(file_path)

    def extract_composable_functions(self, file_path: str) -> List[str]:
        """Composable 함수들을 추출합니다."""
        composables = []
        try:
            with open(file_path, "r", encoding="utf-8") as f:
                content = f.read()
                # @Composable 함수 패턴 찾기
                pattern = r"@Composable\s+fun\s+(\w+)\s*\("
                matches = re.findall(pattern, content)
                composables.extend(matches)
        except Exception as e:
            print(f"파일 읽기 오류 {file_path}: {e}")
        return composables

    def extract_classes(self, file_path: str) -> List[str]:
        """클래스들을 추출합니다."""
        classes = []
        try:
            with open(file_path, "r", encoding="utf-8") as f:
                content = f.read()
                # class 패턴 찾기
                pattern = r"class\s+(\w+)"
                matches = re.findall(pattern, content)
                classes.extend(matches)
        except Exception as e:
            print(f"파일 읽기 오류 {file_path}: {e}")
        return classes

    def extract_functions(self, file_path: str) -> List[str]:
        """함수들을 추출합니다."""
        functions = []
        try:
            with open(file_path, "r", encoding="utf-8") as f:
                content = f.read()
                # fun 패턴 찾기 (Composable 제외)
                pattern = r"fun\s+(\w+)\s*\("
                matches = re.findall(pattern, content)
                # @Composable이 아닌 함수만 필터링
                for match in matches:
                    if not re.search(r"@Composable\s+fun\s+" + match, content):
                        functions.append(match)
        except Exception as e:
            print(f"파일 읽기 오류 {file_path}: {e}")
        return functions

    def find_existing_tests(self) -> Set[str]:
        """기존 테스트에서 테스트되는 항목들을 찾습니다."""
        tested_items = set()
        for test_file in self.test_sources:
            try:
                with open(test_file, "r", encoding="utf-8") as f:
                    content = f.read()

                    # 클래스 테스트 찾기
                    class_pattern = r"class\s+(\w+)Test"
                    class_matches = re.findall(class_pattern, content)
                    for match in class_matches:
                        tested_items.add(match)

                    # 함수 테스트 찾기 (더 정확한 패턴)
                    func_pattern = r"fun\s+test(\w+)"
                    func_matches = re.findall(func_pattern, content)
                    for match in func_matches:
                        tested_items.add(match)

                    # 특정 테스트 대상들 찾기
                    specific_tests = [
                        "SudokuGame",
                        "SudokuViewModel",
                        "SudokuState",
                        "SudokuBoard",
                        "NumberPad",
                        "SudokuScreen",
                    ]
                    for test in specific_tests:
                        if test in content:
                            tested_items.add(test)
            except Exception as e:
                print(f"테스트 파일 읽기 오류 {test_file}: {e}")
        return tested_items

    def analyze_gaps(self):
        """테스트 갭을 분석합니다."""
        print("🔍 테스트 갭 분석 중...")

        # 기존 테스트된 항목들
        tested_items = self.find_existing_tests()

        # 메인 소스에서 테스트 가능한 항목들 찾기
        all_testable_items = set()

        for source_file in self.main_sources:
            if "MainActivity" in source_file:
                continue  # MainActivity는 테스트 제외

            composables = self.extract_composable_functions(source_file)
            classes = self.extract_classes(source_file)
            functions = self.extract_functions(source_file)

            all_testable_items.update(composables)
            all_testable_items.update(classes)
            all_testable_items.update(functions)

        # 누락된 테스트 찾기
        missing_items = all_testable_items - tested_items

        print(f"📊 분석 결과:")
        print(f"   - 전체 테스트 가능 항목: {len(all_testable_items)}")
        print(f"   - 기존 테스트 항목: {len(tested_items)}")
        print(f"   - 누락된 테스트: {len(missing_items)}")

        print(f"\n✅ 기존 테스트 항목들:")
        for item in sorted(tested_items):
            print(f"   - {item}")

        if missing_items:
            print(f"\n❌ 누락된 테스트 항목들:")
            for item in sorted(missing_items):
                print(f"   - {item}")

        return missing_items

    def generate_test_suggestions(self, missing_items: Set[str]):
        """누락된 테스트에 대한 제안을 생성합니다."""
        print(f"\n💡 테스트 생성 제안:")

        suggestions = {
            "UI_Components": [],
            "Business_Logic": [],
            "State_Management": [],
            "Edge_Cases": [],
        }

        for item in missing_items:
            if item in [
                "SudokuCell",
                "NumberButton",
                "ActionButton",
                "New_sudokuTheme",
            ]:
                suggestions["UI_Components"].append(item)
            elif item in ["SudokuGame", "SudokuViewModel"]:
                suggestions["Business_Logic"].append(item)
            elif item in ["SudokuState"]:
                suggestions["State_Management"].append(item)
            else:
                suggestions["Edge_Cases"].append(item)

        for category, items in suggestions.items():
            if items:
                print(f"\n📱 {category}:")
                for item in items:
                    print(f"   - {item} 테스트 추가 필요")

        return suggestions

    def generate_specific_test_recommendations(self):
        """구체적인 테스트 추천을 생성합니다."""
        print(f"\n🎯 구체적인 테스트 추천:")

        recommendations = [
            {
                "category": "UI 컴포넌트 개별 테스트",
                "items": [
                    "SudokuCell - 선택/비선택 상태, 초기값/사용자 입력 구분",
                    "NumberButton - 숫자 버튼 클릭 이벤트",
                    "ActionButton - 지우기 버튼 동작",
                    "SudokuBoard - 전체 보드 렌더링 및 셀 클릭",
                ],
            },
            {
                "category": "상태 변화 테스트",
                "items": [
                    "게임 완료 시 상태 변화",
                    "에러 메시지 표시/숨김",
                    "셀 선택 상태 변화",
                    "보드 초기화 후 상태",
                ],
            },
            {
                "category": "경계값 테스트",
                "items": [
                    "빈 보드에서의 동작",
                    "모든 셀이 채워진 상태",
                    "잘못된 인덱스 접근",
                    "초기값 셀 수정 시도",
                ],
            },
            {
                "category": "통합 시나리오 테스트",
                "items": [
                    "완전한 게임 플레이 시나리오",
                    "해답 보기 후 새 게임",
                    "에러 발생 후 정상 동작 복구",
                    "다양한 숫자 입력 패턴",
                ],
            },
        ]

        for rec in recommendations:
            print(f"\n📋 {rec['category']}:")
            for item in rec["items"]:
                print(f"   - {item}")

    def generate_test_templates(self, suggestions: Dict[str, List[str]]):
        """테스트 템플릿을 생성합니다."""
        print(f"\n📝 테스트 템플릿 생성:")

        for category, items in suggestions.items():
            if not items:
                continue

            print(f"\n--- {category} 테스트 템플릿 ---")

            for item in items:
                if category == "UI_Components":
                    self.generate_ui_test_template(item)
                elif category == "Business_Logic":
                    self.generate_logic_test_template(item)
                elif category == "State_Management":
                    self.generate_state_test_template(item)

    def generate_ui_test_template(self, component_name: str):
        """UI 컴포넌트 테스트 템플릿 생성"""
        template = f"""
@RunWith(AndroidJUnit4::class)
class {component_name}Test {{
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun test{component_name}Display() {{
        composeTestRule.setContent {{
            // TODO: {component_name} 컴포넌트 렌더링
        }}
        
        // TODO: 컴포넌트가 올바르게 표시되는지 확인
    }}
    
    @Test
    fun test{component_name}Interaction() {{
        composeTestRule.setContent {{
            // TODO: {component_name} 컴포넌트 렌더링
        }}
        
        // TODO: 사용자 상호작용 테스트
    }}
}}
"""
        print(f"UI 테스트 템플릿 ({component_name}):")
        print(template)

    def generate_logic_test_template(self, class_name: str):
        """비즈니스 로직 테스트 템플릿 생성"""
        template = f"""
class {class_name}Test {{
    private lateinit var {class_name.lower()}: {class_name}
    
    @Before
    fun setUp() {{
        {class_name.lower()} = {class_name}()
    }}
    
    @Test
    fun test{class_name}Initialization() {{
        // TODO: 초기화 상태 확인
    }}
    
    @Test
    fun test{class_name}CoreFunctionality() {{
        // TODO: 핵심 기능 테스트
    }}
    
    @Test
    fun test{class_name}EdgeCases() {{
        // TODO: 경계값 테스트
    }}
}}
"""
        print(f"로직 테스트 템플릿 ({class_name}):")
        print(template)

    def generate_state_test_template(self, state_name: str):
        """상태 관리 테스트 템플릿 생성"""
        template = f"""
class {state_name}Test {{
    @Test
    fun test{state_name}DataClass() {{
        // TODO: 데이터 클래스 속성들 테스트
    }}
    
    @Test
    fun test{state_name}Equality() {{
        // TODO: 동등성 비교 테스트
    }}
    
    @Test
    fun test{state_name}Copy() {{
        // TODO: copy() 메서드 테스트
    }}
}}
"""
        print(f"상태 테스트 템플릿 ({state_name}):")
        print(template)


def main():
    analyzer = TestGapAnalyzer(".")
    analyzer.scan_project()
    missing_items = analyzer.analyze_gaps()
    suggestions = analyzer.generate_test_suggestions(missing_items)
    analyzer.generate_specific_test_recommendations()
    analyzer.generate_test_templates(suggestions)


if __name__ == "__main__":
    main()
