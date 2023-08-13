# 본 프로젝트의 목적
이 프로젝트는 Tutorial 프로젝트로, 다음에 대한 가이드를 제공하기 위함.
* Actor 사용 방법
    * ask pattern
    * routing pattern
* State machine 을 사용하여 구현 하는 방법
    * state diagram 작성 방법
 


# 코딩 규칙
* 모듈간 직접적인 actor message로 통신하지 않는다.
    * interface(trait)을 선언하고,  다른 모듈에서는 interface 를 사용하여 기능을 호출
    * 모듈 내에서만 actor message 를 이용하여 기능 구현
    * 객체지향의 원칙중, 인터페이스 분리 원칙과 , 의존성 역전 원칙을 지키기 위함. 
* Actor 내부 변수 변경 금지
    * Actor 내부의 변수 값을 직접 바꾸는 것은 권장하지 않는다.
    * 바꿔야 하는 경우, 바뀐값을 가지고 become 하는 것을 권장
```
    // Receive를 리턴하는 함수의 아규먼트에 필요한 변수를 추가
    def retryState(retryCount : Int) : Receive = {
        // retryCount를 1증가 시켜야 하면,  become을 사용
        case Error => context.become(retryState(retryCount + 1))
    }
```

* Context 사용
    * deadline 이 지난 메시지를 폐기하기 위해 context 를 사용하는 것을 권장
    * https://go.dev/blog/context 
* Actor State 파일 분리
    * state 개수가 작다면 ( 4개 이하? ) 굳이 분리하지 않아도 좋지만,
    * state 개수가 많아진다면, 별도의 클래스로 정의하면서 파일을 분리하는 것을 권장.
        * 이때 state의 생성자에 부모  actor의 reference 가 필요. 
        * timer , context , stash 등의 API를 actor가 가지고 있기 때문.
    * 구현 전에 state machine설계가 이루어 진다면,  state 별로 업무 할당이 가능. 



