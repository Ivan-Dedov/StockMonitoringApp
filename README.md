# StocksMonitoringApp

**Доброго времени суток, дорогой проверяющий!**

Вот мое приложение для мониторинга цен акций из S&P 500. Немного информации о фичах, чтобы было проще проверять.

## Основной функционал:
* **На стартовом экране отображаются акции из списка S&P 500.** Список берется с сайта https://www.slickcharts.com/sp500. В разделе *Stocks* акции подгружаются по 10 штук. Когда пользователь прокрутит список до конца вниз, будут постепенно подгружаться еще акции. Цены и информация об акциях могут не показываться (акции могут не загружаться), если совершено более 100 запросов в минуту. По прошествии этой самой минуты информация о незаконченных акциях будет загружена в список, и пользователь снова сможет прокручивать список ниже.
* **Пользователь может добавлять акции в избранные и просматривать этот список.** Для того, чтобы добавить акцию в избранное, необходимо нажать на звездочку справа от тикера. Для просмотра списка избранных акций, необходимо нажать на кнопку *Favourites* вверху экрана. Для удаления акции из избранных нужно снова нажать на звездочку. Информация об избранных акциях при повторном запуске приложения подгружается так, как описано выше - постепенно, батчами. Поэтому не паникуйте, если о каких-то акциях информация будет видна, а по каким-то - нет.
* **Пользователь может искать акции по тикеру или названию.** Чтобы найти акцию, нужно начать вводить символы в строку поиска вверху экрана. Поиск акций осуществляется:
  * *по тикеру*: *начало* тикера должно совпадать с введенным. При вводе "M" в строку поиска акция **M**SFT будет найдена, а акция A**M**ZN - нет;
  * *по названию компании*: символы в строке поиска должны встречаться *где-нибудь* в названии. При вводе "A" будут найдены все компании, у которых в названии (оно пишется снизу от тикера) есть буква A.
  * Эти действия осуществляются одновременно (т.е. **нет** возможности переключаться между поиском по имени компании и по тикеру).

## Дополнительный функционал:
* **Реализован экран с просмотром дополнительной информации об акции.** Чтобы открыть его, нужно нажать на карточку с акцией (куда угодно, кроме звездочки). Это приведет к открытию нового окошка, где указаны: тикер, название компании, ее страна регистрации, область деятельности, капитализация рынка, наибольшая, наименьшая, открытая и текущая цены за день, а также процентное изменение цены за день. Можно было бы добавить больше информации, но ввиду ограничений API (100 запросов в минуту), было решено этого не делать.

## Другая информация:
* В ходе реализации использовался Finnhub API.
* Приложение *не должно* падать в ходе работы.
* Дизайн интерфейса взят с предложенного в условии задания сайта.
* При выходе из приложения сохраняются тикеры акций, входящих в избранное. При повторном запуске они загружаются в приложение. Сохранение идет в память устройства.
* Иногда при запуске приложения акции из списка *Stocks* не показываются. Чтобы их увидеть, нужно переключиться между вкладками *Stocks* и *Favourites*, и обратно. Не знаю, с чем это связано, но, полагаю, что это не критичная ошибка.
* Строка поиска фильтрует результаты в *текущем* окне: если выбрана вкладка *Stocks*, фильтрация производится из всех акций, если выбрана вкладка *Favorites* - то фильтруется только список избранных акций. При наличии текста в строке поиска и переключении между вкладками *Stocks* и *Favourites* список фильтрованных акций *должен* меняться.
* Код отформатирован согласно кодстайлу Java.

**Надеюсь, вам понравится моя работа! Удачной проверки (◕ ω ◕)**
