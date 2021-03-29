# ft_hangouts
This a [School 42](https://www.42.fr/) project to learn the basics of native Android app development. The PDF of the subject is [here](https://cdn.intra.42.fr/pdf/pdf/13208/en.subject.pdf).  
The objective is a simple contacts management app, with SMS messaging. This was my first experience with mobile development. I decided to do it in Kotlin, with a MVVM architecture (using [Codinginflow's great MVVM tutorial](https://www.youtube.com/playlist?list=PLrnPJCHvNZuCfAe7QK2BoMPkv2TGM_b0E) as a starting point).

## Mandatory features
- Contact management (create, update, delete contacts) using a dedicated sqlite database (NOT the system contact table)
- SMS messaging: complete history of the conversation, updated onscreen with every new sent/received SMS
- Fully translated in English and French

## Bonus features
- Set a contact as favorite
- Sorting and search (which can be combined)
- Fully featured themes (not just changing the header's color): Solarized Light, Solarized Dark, OLED
- Adding a picture to a contact: from the camera or from the phone's storage, with permissions managed gracefully
- Dedicated intents to easily write an email/start a call

## Gallery
![HomeScreen](/screenshots/1-HomeScreen.png)