package com.itsmesujan.jlpt.content

import com.itsmesujan.jlpt.domain.Chapter
import com.itsmesujan.jlpt.domain.Exercise
import com.itsmesujan.jlpt.domain.ExerciseType
import com.itsmesujan.jlpt.domain.JlptLevel
import com.itsmesujan.jlpt.domain.LessonNode
import com.itsmesujan.jlpt.domain.NodeKind

/**
 * Bundled launch content — original items written for this app, in JLPT style/format only.
 * NEVER reproduce real JLPT questions (copyright: Japan Foundation / JEES) and never copy
 * textbook or competitor sentences. Vocab/kanji selection follows commonly targeted N5 ranges
 * (the JLPT publishes no official list). Sources for future expansion: JMdict/KANJIDIC2 and
 * KanjiVG (CC BY-SA, attribution in the app's Licences screen), Tatoeba (CC BY, filtered).
 */
object Content {

    private fun mc(id: String, skill: String, prompt: String, sub: String?, options: List<String>, answer: String, why: String, key: String) =
        Exercise(id, ExerciseType.MULTIPLE_CHOICE, skill, prompt, sub, options = options, correctAnswer = answer, explanation = why, itemKey = key)

    private fun typed(id: String, skill: String, prompt: String, sub: String?, answer: String, accepted: List<String>, why: String, key: String) =
        Exercise(id, ExerciseType.TYPED_RETRIEVAL, skill, prompt, sub, correctAnswer = answer, acceptedAnswers = accepted, explanation = why, itemKey = key)

    private fun listen(id: String, spoken: String, prompt: String, options: List<String>, answer: String, why: String, key: String) =
        Exercise(id, ExerciseType.LISTENING, "listening", prompt, null, spokenText = spoken, options = options, correctAnswer = answer, explanation = why, itemKey = key)

    private fun read(id: String, passage: String, prompt: String, options: List<String>, answer: String, why: String, key: String) =
        Exercise(id, ExerciseType.READING, "reading", prompt, null, passage = passage, options = options, correctAnswer = answer, explanation = why, itemKey = key)

    // ------------------------------------------------------------------ Chapter 1
    private val ch1Words = LessonNode(
        id = "n5c1_words", kind = NodeKind.WORD_BATTLE, title = "First Words", subtitle = "Greetings, people and numbers",
        exercises = listOf(
            mc("c1w1", "vocab", "せんせい", "What does this word mean?", listOf("Teacher", "Student", "Doctor", "Friend"), "Teacher", "せんせい (sensei) means teacher. Students are がくせい.", "vocab:せんせい"),
            mc("c1w2", "vocab", "ともだち", "What does this word mean?", listOf("Family", "Friend", "Classmate only", "Neighbour"), "Friend", "ともだち (tomodachi) means friend.", "vocab:ともだち"),
            mc("c1w3", "vocab", "Which word means “water”?", null, listOf("みず", "ひ", "やま", "かわ"), "みず", "みず (mizu) is water. ひ is fire/sun, やま is mountain, かわ is river.", "vocab:みず"),
            mc("c1w4", "vocab", "いぬ", "What does this word mean?", listOf("Cat", "Bird", "Dog", "Fish"), "Dog", "いぬ (inu) is dog. Cat is ねこ.", "vocab:いぬ"),
            mc("c1w5", "vocab", "Which word means “today”?", null, listOf("きのう", "あした", "きょう", "まいにち"), "きょう", "きょう (kyou) is today; きのう yesterday, あした tomorrow.", "vocab:きょう"),
            mc("c1w6", "vocab", "がっこう", "What does this word mean?", listOf("Hospital", "School", "Station", "Shop"), "School", "がっこう (gakkou) is school. Station is えき.", "vocab:がっこう"),
            mc("c1w7", "vocab", "Which word means “three (people)”?", null, listOf("ひとり", "ふたり", "さんにん", "よにん"), "さんにん", "Counters change with people: ひとり, ふたり, then さんにん (3 people).", "vocab:さんにん"),
            typed("c1w8", "vocab", "Type the reading of this word:", "せんせい", "せんせい", listOf("せんせい", "sensei"), "せんせい is read sensei.", "vocab:せんせい")
        )
    )

    private val ch1Kanji = LessonNode(
        id = "n5c1_kanji", kind = NodeKind.KANJI_CLASH, title = "Kanji Foundations", subtitle = "Numbers and nature kanji",
        exercises = listOf(
            mc("c1k1", "kanji", "一", "What is the meaning of this kanji?", listOf("One", "Two", "Three", "Person"), "One", "一 = one, read いち / ひと(つ). Stroke order and compounds live in Kanji Practice.", "kanji:一"),
            mc("c1k2", "kanji", "人", "What is the meaning of this kanji?", listOf("Mountain", "Person", "River", "Tree"), "Person", "人 = person, read じん / にん / ひと. 日本人 (にほんじん) = Japanese person.", "kanji:人"),
            mc("c1k3", "kanji", "Which kanji means “day / sun”?", null, listOf("月", "日", "年", "時"), "日", "日 = day/sun (にち, ひ). 月 is month/moon.", "kanji:日"),
            mc("c1k4", "kanji", "山", "How is this kanji read in 山 (mountain)?", listOf("やま", "かわ", "うみ", "もり"), "やま", "山 alone is read やま (kun'yomi). 富士山 uses the on'yomi さん.", "kanji:山"),
            mc("c1k5", "kanji", "Which kanji means “water”?", null, listOf("火", "木", "水", "金"), "水", "水 = water (すい / みず). 火 fire, 木 tree, 金 gold/money.", "kanji:水"),
            typed("c1k6", "kanji", "Type the kun'yomi reading of 川 (river):", "Use hiragana.", "かわ", listOf("かわ", "kawa"), "川 (river) is read かわ.", "kanji:川")
        )
    )

    private val ch1Grammar = LessonNode(
        id = "n5c1_grammar", kind = NodeKind.GRAMMAR_DOJO, title = "は・です・か", subtitle = "Topic, polite copula and questions",
        exercises = listOf(
            mc("c1g1", "grammar", "わたし ＿ がくせいです。", "Choose the topic particle.", listOf("は", "が", "を", "に"), "は", "は (pronounced wa) marks the topic: わたしは がくせいです — I am a student.", "grammar:は"),
            mc("c1g2", "grammar", "それは ほん ＿。", "Make this a polite statement: “That is a book.”", listOf("です", "ます", "か", "の"), "です", "Noun + です is the polite statement pattern: ほんです.", "grammar:です"),
            mc("c1g3", "grammar", "How do you turn これは みずです into a question?", null, listOf("これは みずかです", "これは みずですか", "これは みずをは", "これは みずでに"), "これは みずですか", "Add か at the end: …ですか makes a polite question. Word order doesn't change.", "grammar:か"),
            mc("c1g4", "grammar", "にほん ＿ ひと", "“Person of Japan” — choose the linking particle.", listOf("は", "の", "か", "へ"), "の", "の links nouns: にほんのひと (a person of Japan).", "grammar:の"),
            typed("c1g5", "grammar", "Type the missing particle: わたし ＿ せんせいです。", "One hiragana character.", "は", listOf("は", "wa"), "Topic particle は, typed は and pronounced wa.", "grammar:は"),
            typed("c1g6", "grammar", "Complete the sentence: これは えんぴつ ＿ 。(polite statement ending)", "Two hiragana characters.", "です", listOf("です", "desu"), "Noun sentences end with です in polite speech.", "grammar:です")
        )
    )

    private val ch1Listening = LessonNode(
        id = "n5c1_listen", kind = NodeKind.ECHO_QUEST, title = "First Sounds", subtitle = "Greetings and classroom phrases",
        exercises = listOf(
            listen("c1l1", "おはようございます", "Listen. What did you hear?", listOf("Good morning", "Good evening", "Thank you", "Excuse me"), "Good morning", "おはようございます is the polite morning greeting.", "listen:greeting_morning"),
            listen("c1l2", "ありがとうございます", "Listen. What did you hear?", listOf("I'm sorry", "Thank you very much", "See you tomorrow", "Welcome"), "Thank you very much", "ありがとうございます = thank you very much (polite).", "listen:arigatou"),
            listen("c1l3", "すみません", "Listen. Which situation fits this phrase?", listOf("Saying goodbye forever", "Getting attention / apologising lightly", "Congratulating someone", "Asking the price"), "Getting attention / apologising lightly", "すみません is used to say excuse me and to call staff.", "listen:sumimasen"),
            listen("c1l4", "わたしは がくせいです", "Listen. What does the speaker say?", listOf("I am a teacher", "I am a student", "That is a school", "This is my friend"), "I am a student", "わたしは がくせいです = I am a student.", "listen:self_intro"),
            listen("c1l5", "いま さんじです", "Listen. What time is it?", listOf("1 o'clock", "2 o'clock", "3 o'clock", "9 o'clock"), "3 o'clock", "さんじ = 3 o'clock. Time uses number + じ.", "listen:time_3")
        )
    )

    private val ch1ReadingPassage = "はじめまして。わたしは アンナです。ドイツの ひとです。いま、にほんの がっこうで にほんごを べんきょうします。せんせいは たなかせんせいです。たなかせんせいは にほんじんです。わたしの ともだちは カルロスさんです。カルロスさんは スペインの ひとです。"

    private val ch1Reading = LessonNode(
        id = "n5c1_reading", kind = NodeKind.STORY_TRAIL, title = "Meet Anna", subtitle = "A short self-introduction",
        exercises = listOf(
            read("c1r1", ch1ReadingPassage, "Where is Anna from?", listOf("Spain", "Germany", "Japan", "Italy"), "Germany", "ドイツのひと — Anna is a person of Germany (ドイツ).", "reading:anna_origin"),
            read("c1r2", ch1ReadingPassage, "What does Anna study?", listOf("English", "History", "Japanese", "Math"), "Japanese", "にほんごを べんきょうします — she studies Japanese.", "reading:anna_study"),
            read("c1r3", ch1ReadingPassage, "Who is Tanaka-sensei?", listOf("Anna's friend", "Anna's teacher, a Japanese person", "A student from Spain", "Anna's family"), "Anna's teacher, a Japanese person", "せんせいは たなかせんせいです。たなかせんせいは にほんじんです。", "reading:tanaka"),
            read("c1r4", ch1ReadingPassage, "Where is Carlos from?", listOf("Germany", "Japan", "Spain", "France"), "Spain", "カルロスさんは スペインのひとです — Carlos is from Spain.", "reading:carlos")
        )
    )

    private val ch1Boss = LessonNode(
        id = "n5c1_boss", kind = NodeKind.CHAPTER_BOSS, title = "Arrival Boss", subtitle = "10-question mini mock — prove Chapter 1",
        isBoss = true, timeLimitSeconds = 300,
        exercises = listOf(
            mc("c1b1", "vocab", "えき", "Meaning?", listOf("Station", "Shop", "Park", "Bank"), "Station", "えき = station.", "vocab:えき"),
            mc("c1b2", "kanji", "月", "Meaning?", listOf("Day", "Month / moon", "Year", "Time"), "Month / moon", "月 = month/moon (げつ / つき).", "kanji:月"),
            mc("c1b3", "grammar", "かれは いしゃ ＿。", "Polite statement ending.", listOf("です", "かは", "をに", "ますか"), "です", "Noun + です.", "grammar:です"),
            mc("c1b4", "vocab", "Which word means “book”?", null, listOf("ほん", "いす", "つくえ", "かぎ"), "ほん", "ほん = book.", "vocab:ほん"),
            listen("c1b5", "こんばんは", "Listen. What did you hear?", listOf("Good morning", "Good evening", "Good night (to family)", "Hello (on the phone)"), "Good evening", "こんばんは = good evening.", "listen:konbanwa"),
            mc("c1b6", "grammar", "これ ＿ わたしの かさです。", "Topic particle.", listOf("は", "を", "へ", "と"), "は", "これは… marks これ as the topic.", "grammar:は"),
            mc("c1b7", "kanji", "Which kanji means “tree”?", null, listOf("木", "本", "森川", "花"), "木", "木 = tree (き / もく). 本 also contains 木 but means book/origin.", "kanji:木"),
            read("c1b8", ch1ReadingPassage, "According to the passage, Anna studies at…", listOf("A company in Germany", "A school in Japan", "A university in Spain", "Home only"), "A school in Japan", "にほんの がっこうで べんきょうします — at a school in Japan.", "reading:anna_school"),
            mc("c1b9", "vocab", "あした", "Meaning?", listOf("Yesterday", "Today", "Tomorrow", "Every day"), "Tomorrow", "あした = tomorrow.", "vocab:あした"),
            listen("c1b10", "これは いくらですか", "Listen. What is the speaker asking?", listOf("Where is this?", "How much is this?", "What time is it?", "Whose is this?"), "How much is this?", "いくらですか asks the price — a phrase you'll use daily in Japan.", "listen:ikura")
        )
    )

    // ------------------------------------------------------------------ Chapter 2
    private fun marketWords() = LessonNode(
        id = "n5c2_words", kind = NodeKind.WORD_BATTLE, title = "At the Market", subtitle = "Food and shopping words",
        exercises = listOf(
            mc("c2w1", "vocab", "たべもの", "Meaning?", listOf("Drink", "Food", "Fruit", "Vegetable"), "Food", "たべもの = food (from たべる, to eat).", "vocab:たべもの"),
            mc("c2w2", "vocab", "Which word means “expensive”?", null, listOf("やすい", "たかい", "おいしい", "あまい"), "たかい", "たかい = expensive (also tall/high). やすい = cheap.", "vocab:たかい"),
            mc("c2w3", "vocab", "りんご", "Meaning?", listOf("Orange", "Grape", "Apple", "Peach"), "Apple", "りんご = apple.", "vocab:りんご"),
            mc("c2w4", "vocab", "Which word means “shop / store”?", null, listOf("みせ", "へや", "まち", "むら"), "みせ", "みせ = shop. へや room, まち town.", "vocab:みせ"),
            typed("c2w5", "vocab", "Type the reading: たべもの", null, "たべもの", listOf("たべもの", "tabemono"), "たべもの — food.", "vocab:たべもの")
        )
    )

    private fun marketKanji() = LessonNode(
        id = "n5c2_kanji", kind = NodeKind.KANJI_CLASH, title = "Money & Size Kanji", subtitle = "円・大・小・円 and friends",
        exercises = listOf(
            mc("c2k1", "kanji", "円", "Meaning?", listOf("Yen / circle", "Book", "Car", "Electricity"), "Yen / circle", "円 = yen / circle (えん). Prices in Japan are written with 円.", "kanji:円"),
            mc("c2k2", "kanji", "大", "Meaning?", listOf("Small", "Big", "Middle", "Outside"), "Big", "大 = big (だい / おおきい).", "kanji:大"),
            mc("c2k3", "kanji", "Which kanji means “small”?", null, listOf("小", "少", "中", "上"), "小", "小 = small (しょう / ちいさい).", "kanji:小"),
            mc("c2k4", "kanji", "店", "Meaning?", listOf("Station", "Shop / store", "School", "Company"), "Shop / store", "店 = shop (てん / みせ), as in 本屋's cousin てんいん (clerk).", "kanji:店"),
            typed("c2k5", "kanji", "Type the on'yomi reading of 円 as in お金 (money context): えん / ゑん?", null, "えん", listOf("えん", "en"), "円 is read えん when it means yen.", "kanji:円")
        )
    )

    private fun marketGrammar() = LessonNode(
        id = "n5c2_grammar", kind = NodeKind.GRAMMAR_DOJO, title = "を・ください", subtitle = "Object particle and polite requests",
        exercises = listOf(
            mc("c2g1", "grammar", "みず ＿ のみます。", "Choose the object particle.", listOf("を", "は", "に", "へ"), "を", "を marks the direct object: みずを のみます — drink water.", "grammar:を"),
            mc("c2g2", "grammar", "How do you politely ask for an item at a shop?", null, listOf("これを ください", "これは ですか", "これに ます", "これと です"), "これを ください", "Noun + をください = “this one, please” — the core shopping pattern.", "grammar:ください"),
            mc("c2g3", "grammar", "りんご ＿ たべます。", "Object particle.", listOf("を", "が", "の", "か"), "を", "りんごを たべます — eat an apple.", "grammar:を"),
            typed("c2g4", "grammar", "Type the missing word: これを ＿ 。(“this one, please”)", null, "ください", listOf("ください", "kudasai"), "ください completes the polite request pattern.", "grammar:ください"),
            mc("c2g5", "grammar", "Which sentence means “I buy a book”?", null, listOf("ほんを かいます", "ほんは かいです", "ほんに かいます", "ほんか います"), "ほんを かいます", "Object + を + verb in ます-form: ほんを かいます.", "grammar:を")
        )
    )

    private fun marketListening() = LessonNode(
        id = "n5c2_listen", kind = NodeKind.ECHO_QUEST, title = "Shop Sounds", subtitle = "Prices and requests",
        exercises = listOf(
            listen("c2l1", "いらっしゃいませ", "Listen. Where would you hear this?", listOf("In a classroom exam", "Entering a shop", "On a train platform only", "At a shrine at midnight"), "Entering a shop", "いらっしゃいませ is the staff welcome in shops and restaurants.", "listen:irasshai"),
            listen("c2l2", "ぜんぶで せんえんです", "Listen. How much is it in total?", listOf("100 yen", "500 yen", "1,000 yen", "10,000 yen"), "1,000 yen", "せんえん = 1,000 yen; ぜんぶで = in total.", "listen:senen"),
            listen("c2l3", "これを ください", "Listen. What does the customer want?", listOf("A receipt only", "This item, please", "To see the menu", "The way to the station"), "This item, please", "これをください = this one, please.", "listen:kore_kudasai"),
            listen("c2l4", "たかいですね", "Listen. What is the speaker saying?", listOf("It's cheap, isn't it", "It's expensive, isn't it", "It's delicious, isn't it", "It's big, isn't it"), "It's expensive, isn't it", "たかい = expensive; ね seeks agreement.", "listen:takai_ne"),
            listen("c2l5", "ありがとうございました", "Listen. When is this said?", listOf("Before a meal", "Thanking for something already done", "Asking a question", "Apologising"), "Thanking for something already done", "ございました (past) thanks someone for a completed kindness — common when leaving a shop.", "listen:arigatou_past")
        )
    )

    private val marketPassage = "きょうは 土曜日です。わたしは みせで りんごを かいます。りんごは 一つ ひゃくえんです。三つ かいます。ぜんぶで さんびゃくえんです。たなかさんは みかんを かいます。みかんは やすいです。"

    private fun marketReading() = LessonNode(
        id = "n5c2_reading", kind = NodeKind.STORY_TRAIL, title = "Saturday Shopping", subtitle = "Prices in a short passage",
        exercises = listOf(
            read("c2r1", marketPassage, "How much is one apple?", listOf("100 yen", "300 yen", "1,000 yen", "Free"), "100 yen", "一つ ひゃくえん — one apple is 100 yen.", "reading:apple_price"),
            read("c2r2", marketPassage, "How many apples does the writer buy?", listOf("One", "Two", "Three", "Five"), "Three", "三つ かいます — buys three, 300 yen total.", "reading:apple_count"),
            read("c2r3", marketPassage, "What does Tanaka buy?", listOf("Apples", "Mikan (mandarins)", "Books", "Water"), "Mikan (mandarins)", "たなかさんは みかんを かいます.", "reading:tanaka_buys"),
            read("c2r4", marketPassage, "What day is it?", listOf("Monday", "Friday", "Saturday", "Sunday"), "Saturday", "きょうは 土曜日です.", "reading:saturday")
        )
    )

    private fun marketBoss() = LessonNode(
        id = "n5c2_boss", kind = NodeKind.CHAPTER_BOSS, title = "Market Boss", subtitle = "8-question mini mock",
        isBoss = true, timeLimitSeconds = 240,
        exercises = listOf(
            mc("c2b1", "vocab", "やすい", "Meaning?", listOf("Cheap", "Expensive", "Sweet", "Spicy"), "Cheap", "やすい = cheap.", "vocab:やすい"),
            mc("c2b2", "kanji", "店", "Meaning?", listOf("Shop", "Station", "Town", "Room"), "Shop", "店 = shop.", "kanji:店"),
            mc("c2b3", "grammar", "パンを ＿ 。(to eat — choose the right verb form start)", null, listOf("たべます", "たべです", "たべか", "たべを"), "たべます", "Polite present of たべる is たべます.", "grammar:ます"),
            listen("c2b4", "ごちそうさまでした", "Listen. When do you say this?", listOf("Before eating", "After a meal, to thank the cook/host", "When entering a shop", "When paying"), "After a meal, to thank the cook/host", "ごちそうさまでした is said after eating.", "listen:gochisou"),
            read("c2b5", marketPassage, "Total paid for the apples:", listOf("100 yen", "200 yen", "300 yen", "600 yen"), "300 yen", "ぜんぶで さんびゃくえん — 300 yen in total.", "reading:apple_total"),
            mc("c2b6", "vocab", "Which word means “delicious”?", null, listOf("おいしい", "むずかしい", "いそがしい", "あたらしい"), "おいしい", "おいしい = delicious.", "vocab:おいしい"),
            mc("c2b7", "grammar", "これ ＿ ください。", "Object particle before ください.", listOf("を", "は", "に", "の"), "を", "これを ください.", "grammar:を"),
            listen("c2b8", "すみません、えきは どこですか", "Listen. What is being asked?", listOf("The price", "Where the station is", "The time", "A phone number"), "Where the station is", "えきは どこですか = where is the station?", "listen:eki_doko")
        )
    )

    // ------------------------------------------------------------------ Chapter 3
    private fun harborWords() = LessonNode(
        id = "n5c3_words", kind = NodeKind.WORD_BATTLE, title = "On the Move", subtitle = "Travel, time and days",
        exercises = listOf(
            mc("c3w1", "vocab", "でんしゃ", "Meaning?", listOf("Bus", "Train", "Taxi", "Bicycle"), "Train", "でんしゃ = (electric) train.", "vocab:でんしゃ"),
            mc("c3w2", "vocab", "Which word means “tomorrow”?", null, listOf("きのう", "あした", "きょう", "おととい"), "あした", "あした = tomorrow.", "vocab:あした"),
            mc("c3w3", "vocab", "ひる", "Meaning?", listOf("Morning", "Noon / daytime", "Night", "Evening"), "Noon / daytime", "ひる = noon/daytime. あさ morning, よる night.", "vocab:ひる"),
            mc("c3w4", "vocab", "Which word means “to go”?", null, listOf("くる", "いく", "かえる", "ある"), "いく", "いく = to go; くる = to come; かえる = to return.", "vocab:いく"),
            typed("c3w5", "vocab", "Type the reading: でんしゃ", null, "でんしゃ", listOf("でんしゃ", "densha"), "でんしゃ — train.", "vocab:でんしゃ")
        )
    )

    private fun harborKanji() = LessonNode(
        id = "n5c3_kanji", kind = NodeKind.KANJI_CLASH, title = "Time Kanji", subtitle = "時・分・年・月・日",
        exercises = listOf(
            mc("c3k1", "kanji", "時", "Meaning?", listOf("Time / hour", "Minute", "Week", "Year"), "Time / hour", "時 = time/hour (じ / とき). さんじ = 3 o'clock.", "kanji:時"),
            mc("c3k2", "kanji", "分", "Meaning in time expressions?", listOf("Hour", "Minute", "Day", "Month"), "Minute", "分 = minute (ふん / ぷん) in time, also “part”.", "kanji:分"),
            mc("c3k3", "kanji", "Which kanji means “year”?", null, listOf("年", "午", "毎", "今"), "年", "年 = year (ねん / とし).", "kanji:年"),
            mc("c3k4", "kanji", "今", "Meaning?", listOf("Before", "Now", "Later", "Again"), "Now", "今 = now (いま).", "kanji:今"),
            typed("c3k5", "kanji", "Type the reading of 今 (now) in hiragana:", null, "いま", listOf("いま", "ima"), "今 is read いま on its own.", "kanji:今")
        )
    )

    private fun harborGrammar() = LessonNode(
        id = "n5c3_grammar", kind = NodeKind.GRAMMAR_DOJO, title = "に・へ・で", subtitle = "Time, direction and place of action",
        exercises = listOf(
            mc("c3g1", "grammar", "七時 ＿ おきます。", "Time particle for a specific clock time.", listOf("に", "で", "を", "と"), "に", "Specific times take に: 七時に おきます — wake up at 7.", "grammar:に_time"),
            mc("c3g2", "grammar", "がっこう ＿ いきます。", "Direction particle (to school).", listOf("へ", "を", "の", "か"), "へ", "へ (pronounced e) marks direction: がっこうへ いきます. に also works for destination.", "grammar:へ"),
            mc("c3g3", "grammar", "としょかんで べんきょうします。", "Which particle marks the place where studying happens (in the full sentence)?", listOf("で in としょかんで", "に in べんきょうに", "を in しますを", "は in としょかんは"), "で in としょかんで", "で marks the place of action: としょかんで べんきょうします — study at the library.", "grammar:で_place"),
            typed("c3g4", "grammar", "Type the particle: 日曜日 ＿ うちで やすみます — no particle is needed for relative time words; type なし if none.", "Hint: days like 日曜日 take no に.", "なし", listOf("なし", "none"), "Relative time words (today, tomorrow, Sundays in general) usually take no に.", "grammar:に_time"),
            mc("c3g5", "grammar", "Which sentence means “I go to Japan”?", null, listOf("にほんへ いきます", "にほんを いきます", "にほんの いきです", "にほんか いきます"), "にほんへ いきます", "Destination + へ + いきます.", "grammar:へ")
        )
    )

    private fun harborListening() = LessonNode(
        id = "n5c3_listen", kind = NodeKind.ECHO_QUEST, title = "Station Sounds", subtitle = "Times and directions",
        exercises = listOf(
            listen("c3l1", "でんしゃは 九時です", "Listen. When is the train?", listOf("7 o'clock", "8 o'clock", "9 o'clock", "10 o'clock"), "9 o'clock", "九時 = 9 o'clock (くじ).", "listen:train_9"),
            listen("c3l2", "まっすぐ いってください", "Listen. What should you do?", listOf("Turn left", "Go straight, please", "Stop here", "Come back tomorrow"), "Go straight, please", "まっすぐ = straight ahead.", "listen:massugu"),
            listen("c3l3", "ひだりです", "Listen. Which side?", listOf("Right", "Left", "Straight", "Behind"), "Left", "ひだり = left; みぎ = right.", "listen:hidari"),
            listen("c3l4", "あしたは あめです", "Listen. Tomorrow's weather?", listOf("Sunny", "Rain", "Snow", "Windy"), "Rain", "あめ = rain; あしたは あめです — tomorrow will rain.", "listen:ame"),
            listen("c3l5", "えきまで 五分です", "Listen. How long to the station?", listOf("5 minutes", "10 minutes", "15 minutes", "50 minutes"), "5 minutes", "五分 = 5 minutes (ごふん).", "listen:gofun")
        )
    )

    private val harborPassage = "わたしは まいあさ 六時に おきます。七時に あさごはんを たべます。それから、でんしゃで がっこうへ いきます。がっこうは 八時半に はじまります。よる 九時に うちへ かえります。"

    private fun harborReading() = LessonNode(
        id = "n5c3_reading", kind = NodeKind.STORY_TRAIL, title = "My Day", subtitle = "A daily routine passage",
        exercises = listOf(
            read("c3r1", harborPassage, "What time does the writer wake up?", listOf("6:00", "7:00", "8:30", "9:00"), "6:00", "六時に おきます — wakes at six.", "reading:wake"),
            read("c3r2", harborPassage, "How does the writer go to school?", listOf("By bus", "By train", "On foot", "By car"), "By train", "でんしゃで がっこうへ いきます — by train.", "reading:train_go"),
            read("c3r3", harborPassage, "When does school start?", listOf("7:00", "8:00", "8:30", "9:30"), "8:30", "八時半に はじまります — starts at 8:30 (半 = half past).", "reading:start_830"),
            read("c3r4", harborPassage, "What time does the writer return home?", listOf("8:00 pm", "9:00 pm", "10:00 pm", "6:00 pm"), "9:00 pm", "よる 九時に うちへ かえります.", "reading:home_9")
        )
    )

    private fun harborBoss() = LessonNode(
        id = "n5c3_boss", kind = NodeKind.CHAPTER_BOSS, title = "Harbor Boss", subtitle = "8-question mini mock",
        isBoss = true, timeLimitSeconds = 240,
        exercises = listOf(
            mc("c3b1", "vocab", "くるま", "Meaning?", listOf("Car", "Boat", "Plane", "Road"), "Car", "くるま = car.", "vocab:くるま"),
            mc("c3b2", "kanji", "今", "Meaning?", listOf("Now", "Yesterday", "Week", "Morning"), "Now", "今 = now.", "kanji:今"),
            mc("c3b3", "grammar", "八時 ＿ でんしゃに のります。", "Time particle.", listOf("に", "を", "の", "と"), "に", "Clock times take に.", "grammar:に_time"),
            listen("c3b4", "みぎに まがってください", "Listen. What should you do?", listOf("Turn right, please", "Turn left, please", "Go straight", "Stop, please"), "Turn right, please", "みぎに まがる = turn right.", "listen:migi_turn"),
            read("c3b5", harborPassage, "The writer eats breakfast at…", listOf("6:00", "7:00", "8:30", "9:00"), "7:00", "七時に あさごはんを たべます.", "reading:breakfast_7"),
            mc("c3b6", "vocab", "Which word means “evening”?", null, listOf("ゆうがた", "あさ", "ひる", "まよなか"), "ゆうがた", "ゆうがた = early evening.", "vocab:ゆうがた"),
            mc("c3b7", "grammar", "としょかん ＿ ほんを よみます。", "Place-of-action particle.", listOf("で", "に", "を", "か"), "で", "Place of action takes で.", "grammar:で_place"),
            listen("c3b8", "きょうは さむいです", "Listen. Today is…?", listOf("Hot", "Cold", "Warm", "Rainy"), "Cold", "さむい = cold (weather).", "listen:samui")
        )
    )

    val chapters: List<Chapter> = listOf(
        Chapter("n5_ch1", JlptLevel.N5, "Arrival Town", "First steps in Japan", listOf(ch1Words, ch1Kanji, ch1Grammar, ch1Listening, ch1Reading, ch1Boss)),
        Chapter("n5_ch2", JlptLevel.N5, "Market Village", "Food, shops and prices", listOf(marketWords(), marketKanji(), marketGrammar(), marketListening(), marketReading(), marketBoss())),
        Chapter("n5_ch3", JlptLevel.N5, "Harbor Town", "Travel and daily routine", listOf(harborWords(), harborKanji(), harborGrammar(), harborListening(), harborReading(), harborBoss()))
    )

    /** Later levels are modelled as locked worlds on the same engine — content ships in waves. */
    val lockedLevels: List<JlptLevel> = listOf(JlptLevel.N4, JlptLevel.N3, JlptLevel.N2, JlptLevel.N1)

    fun node(nodeId: String): LessonNode? = chapters.flatMap { it.nodes }.firstOrNull { it.id == nodeId }
    fun chapterOf(nodeId: String): Chapter? = chapters.firstOrNull { ch -> ch.nodes.any { it.id == nodeId } }

    /** 12-item placement set spanning kana-era basics to lower-N4 (used by the placement flow). */
    val placement: List<Exercise> = listOf(
        ch1Words.exercises[0], ch1Kanji.exercises[0], ch1Grammar.exercises[0],
        ch1Listening.exercises[0], ch1Words.exercises[4], ch1Grammar.exercises[3],
        marketGrammar().exercises[0], marketWords().exercises[1], harborGrammar().exercises[0],
        harborKanji().exercises[0], marketListening().exercises[1], harborListening().exercises[0]
    )
}
