spring batch에서 chunk 사이즈는 reader로부터 몇개의 데이터를 읽어올지를 말한다.
chunk 사이즈만큼 reader로 부터 데이터를 읽어냈따면 processor로 reader에있는 데이터를 1개씩 꺼내서 처리하고 모두 모아서 wirter를 호출하는 개념.
그리고 나서 다시 reader를 호출하는대 reader가 null을 리턴시 모든 과정이 종료됨.

참고로 reader에서주는 데이터는 Map<string,List<String>> 이런꼴을 1개씩 줄수도있다.
즉 cardinality가 어느정도 들은 데이터는 저렇게 key단위로 묶어서 제공할수있고, 그 key의 종류갯수 자체가 chunk사이즈가 될수있다는말
파티셔너의 grid size나 step의 chunksize는 각각 stepscope를 바탕으로 동적 설정이 가능하므로 알아두자.

item stream reader는 기본 메서드로 open,read,close,update가 존재한다.
open은  step시작시에 작동, read는 chunk버퍼를 채우기위해서 불러올때, update는 chunk단위로 모든 작업이 종료되엇을때(read->processor->witer) 기준값을 설정하고,close는 step이 종료될때 즉 reader에서 읽어온 데이터가 더이상 존재하지않는 경우에 작동한다.
보통 cloose는 resoures관리 용으로쓸거같긴한대 chunk하고 같이 쓸경우 어차피 chunk레벨로 트랜잭션이 관리되다 보니까 굳이 뭘할일은 없을듯? log.info로 데이터를 남긴다던가는 할수도잇을듯.

spring batch의 트랜잭션은=-> chunk단위로 관리된다.
https://pooney.tistory.com/135

해당 포스트 참조.
