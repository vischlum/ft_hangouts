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
<img src="https://github.com/vischlum/ft_hangouts/blob/master/screenshots/1-HomeScreen.png" height="400">
<img src="https://github.com/vischlum/ft_hangouts/blob/master/screenshots/2-SearchSort.png" height="400">
<img src="https://github.com/vischlum/ft_hangouts/blob/master/screenshots/3-ContactDetails.png" height="400">
<img src="https://github.com/vischlum/ft_hangouts/blob/master/screenshots/4-ContactDetailsLandscape.png" height="200">
<img src="https://github.com/vischlum/ft_hangouts/blob/master/screenshots/5-SettingsSolarizedLight.png" height="200">
<img src="https://github.com/vischlum/ft_hangouts/blob/master/screenshots/6-CameraPermissions.png" height="200">
<img src="https://github.com/vischlum/ft_hangouts/blob/master/screenshots/7-Messaging.png" height="400">
<img src="https://github.com/vischlum/ft_hangouts/blob/master/screenshots/8-NewContact.png" height="400">
