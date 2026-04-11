spring batch에서 chunk 사이즈는 reader로부터 몇개의 데이터를 읽어올지를 말한다.
chunk 사이즈만큼 reader로 부터 데이터를 읽어냈따면 processor로 reader에있는 데이터를 1개씩 꺼내서 처리하고 모두 모아서 wirter를 호출하는 개념.
그리고 나서 다시 reader를 호출하는대 reader가 null을 리턴시 모든 과정이 종료됨.

참고로 reader에서주는 데이터는 Map<string,List<String>> 이런꼴을 1개씩 줄수도있다.
즉 cardinality가 어느정도 들은 데이터는 저렇게 key단위로 묶어서 제공할수있고, 그 key의 종류갯수 자체가 chunk사이즈가 될수있다는말
파티셔너의 grid size나 step의 chunksize는 각각 stepscope를 바탕으로 동적 설정이 가능하므로 알아두자.
