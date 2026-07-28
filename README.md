# KezzStreamer
simplest stream logic for minecraft servers

## CONFIG
```
# Настройки плагина KezStreamer
settings:
  # Интервал между рекламными сообщениями (в минутах)
  ad-interval-minutes: 10
  # Задержка перед автоматической остановкой стрима при оффлайн (в минутах)
  offline-timer-minutes: 5
  # Задержка между стримами (в секундах)
  stream-cooldown-seconds: 60

# Сообщения
messages:
  # Сообщение при начале стрима
  stream-start: "&6&lИгрок &e{player} &6&lначал стрим! &7(Группа: {group})"

  # Сообщение при ручной остановке стрима
  stream-stopped-manual: "&6&lСтрим игрока &e{player} &6&lзавершен"

  # Сообщение при автоматической остановке стрима
  stream-stopped: "&6&lСтрим игрока &e{player} &6&lавтоматически завершен из-за отсутствия"

  # Рекламное сообщение ([link] заменяется на кликабельную ссылку)
  stream-ad: "&eСейчас стримит {player} [link]"

  # Текст ссылки в рекламных сообщениях
  link-text: "&6&l[СМОТРЕТЬ]"

  # Сообщение при задержке
  cooldown: "&cПодождите еще {time} секунд!"

  # Заголовок меню /media
  media-header: "&6&l▬▬▬▬▬▬▬▬▬▬▬▬ &5&lACTIVE STREAMS &6&l▬▬▬▬▬▬▬▬▬▬▬▬"

  # Если нет активных стримов
  media-no-streams: "&cНет активных стримов."

  # Формат отображения стрима в /media
  media-stream-format: "&e{player} &7- "

  # Текст кликабельной ссылки в /media
  media-click-text: "&6&l[СМОТРЕТЬ]"

  # Нижний колонтитул меню /media
  media-footer: "&6&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
```
