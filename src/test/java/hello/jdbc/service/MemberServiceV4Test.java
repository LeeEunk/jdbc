package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV4_1;
import hello.jdbc.repository.MemberRepositoryV4_2;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
* 예외 누수 문제 해결
* SQLException 제거
* MemberRepository 인터페이스 의존
* 
* 체크 예외를 런타임 예외로 변환하면서 인터페이스와 서비스 계층의 순수성을 유지할 수 있게 됨
* */

@Slf4j
@SpringBootTest // bean 주입, 스프링 AOP를 적용하려면 스프링 컨테이너가 필요함 -> 해당 애노테이션을 써서 컨테이너 생성
class MemberServiceV4Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";


    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberServiceV4 memberService;

    @TestConfiguration
    static class TestConfig {
        private final DataSource dataSource;

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepository memberRepository() {
            //return new MemberRepositoryV4_1(dataSource); // 단순 예외 변환
            return new MemberRepositoryV4_2(dataSource);
        }

        @Bean
        MemberServiceV4 memberServiceV4() {
            return new MemberServiceV4(memberRepository());
        }
    }
    

    @AfterEach
    void after() {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    void AppCheck() {
        log.info("memberService class={}", memberService.getClass()); // transaction 프록시 class=class hello.jdbc.service.MemberServiceV3_3$$SpringCGLIB$$0 상수값 확인
        log.info("memberRepository class={}", memberRepository.getClass());
        // AOP 프록시 적용 확인
        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer(){
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);

        memberRepository.save(memberA);
        memberRepository.save(memberB);

        //when
        log.info("START TX");
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        log.info("END TX");


        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx()  {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEX = new Member(MEMBER_EX, 10000);

        memberRepository.save(memberA);
        memberRepository.save(memberEX);

        //when
        assertThatThrownBy(()-> memberService.accountTransfer(memberA.getMemberId(), memberEX.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);


        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEX.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(10000); // 이체 중 예외 발생으로 롤백이 수행했으므로
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }
    
}