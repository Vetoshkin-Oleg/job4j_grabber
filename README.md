# job4j_grabber
# Агрегатор вакансий  

Система запускается по расписанию - раз в минуту.  
Период запуска указан в настройках - app.properties. 

Первый сайт будет career.habr.com. Работаем с разделом https://career.habr.com/vacancies/java_developer.  
Программа считывает все вакансии c первых 5 страниц, относящихся к Java, и записывает их в базу.
