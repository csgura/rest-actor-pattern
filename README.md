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
* Actor 내부 변수 변경 금지
    * Actor 내부의 변수 값을 직접 바꾸는 것은 권장하지 않는다.
    * 바꿔야 하는 경우, 바뀐값을 가지고 become 하는 것을 권장
* Context 사용
    * deadline 이 지난 메시지를 폐기하기 위해 context 를 사용하는 것을 권장
    * https://go.dev/blog/context 

```
    // Receive를 리턴하는 함수의 아규먼트에 필요한 변수를 추가
    def retryState(retryCount : Int) : Receive = {
        // retryCount를 1증가 시켜야 하면,  become을 사용
        case Error => context.become(retryState(retryCount + 1))
    }
```

