# German Through Lyrics

The app enables learning aspects of German grammar through listening to German music. The grammatical constructions that the learners of German experience most difficulties with - prepositional phrases, finite verb forms, verbs with prefixes and verbs in the passive voice - were extracted from song texts used in the app. In the highlight mode of listening to a song, these grammatical constructions are highlighted in the song text, which draws the attention of the learner to the structure of constructions.

These grammatical constructions are also used as basis for the multiple-choice questions in the game mode of listening to a song. Through answering the questions, the user practices using the constructions and learns to build them in a right way. Answer options are filled with a variety of automatically generated distractors.

Apart from the highlight mode and the game mode, a karaoke mode where the user can listen to a song and follow the synchronized song text is offered. The search through all the songs in the app is possible and includes filters for song genres and types of grammatical constructions found in song texts.

### Sources used

Song texts were automatically downloaded from genius.com. The song audios and videos are played with YouTube.

### Languages used

- Python: downloading of song texts, extracting grammatical constructions, generating distractors for multiple-choice questions
- SQL: creating a database containing song texts, information about them (e.g. artist and genre) and constructions of different types found in song texts, along with the right options and the distractors for multiple-choice questions
- Java: mobile app backend
- XML: mobile app frontend
