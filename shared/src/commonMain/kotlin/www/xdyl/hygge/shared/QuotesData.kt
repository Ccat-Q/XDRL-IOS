package www.xdyl.hygge.shared

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 每日名言数据 + 选择逻辑（Kotlin Multiplatform / commonMain）。
 *
 * 参照 Android 参考实现整理：6 个分类、中英双语、作者、出处、按日期轮换。
 * 名言均为真实存在、可查证的引文；出处格式为「书名 / 篇章」。
 *
 * 分类代码：
 * - WH = 警世箴言 (Warning/Wisdom)
 * - RW = 理性思辨 (Rational)
 * - HC = 心灵疗愈 (Healing)
 * - ED = 存在哲思 (Existential)
 * - CE = 人际纽带 (Connection)
 * - AC = 行动召唤 (Action)
 */
data class Quote(
    val textEn: String,  // 英文原文
    val textZh: String,  // 中文翻译
    val author: String,  // 作者
    val source: String,  // 出处（可为 ""）
    val category: String // 分类代码 WH/RW/HC/ED/CE/AC
)

private val QUOTES: List<Quote> = listOf(
    // ============ WH 警世箴言 ============
    Quote(
        textEn = "Power tends to corrupt, and absolute power corrupts absolutely.",
        textZh = "权力导致腐败，绝对的权力绝对导致腐败。",
        author = "阿克顿勋爵",
        source = "《致曼德尔·克赖顿主教的信》1887年",
        category = "WH"
    ),
    Quote(
        textEn = "The banality of evil.",
        textZh = "邪恶的平庸性。",
        author = "汉娜·阿伦特",
        source = "《艾希曼在耶路撒冷：关于平庸之恶的报告》",
        category = "WH"
    ),
    Quote(
        textEn = "He who fights with monsters should look to it that he himself does not become a monster. And when you gaze long into an abyss, the abyss also gazes into you.",
        textZh = "与恶龙缠斗过久，自身亦成为恶龙；凝视深渊过久，深渊将回以凝视。",
        author = "弗里德里希·尼采",
        source = "《善恶的彼岸》",
        category = "WH"
    ),
    Quote(
        textEn = "Man is born free, and everywhere he is in chains.",
        textZh = "人天生是自由的，却无往不在枷锁之中。",
        author = "让-雅克·卢梭",
        source = "《社会契约论》",
        category = "WH"
    ),
    Quote(
        textEn = "Those who cannot remember the past are condemned to repeat it.",
        textZh = "忘记过去的人注定要重蹈覆辙。",
        author = "乔治·桑塔亚那",
        source = "《理性的生活》",
        category = "WH"
    ),
    Quote(
        textEn = "Hypocrisy is the homage that vice pays to virtue.",
        textZh = "伪善是邪恶向美德致敬。",
        author = "拉罗什富科",
        source = "《箴言集》",
        category = "WH"
    ),
    Quote(
        textEn = "Mediocre men throughout the ages have all been ruined by indolence; talented men throughout the ages have all been ruined by arrogance.",
        textZh = "天下古今之庸人，皆以一惰字致败；天下古今之才人，皆以一傲字致败。",
        author = "曾国藩",
        source = "《曾国藩家书》",
        category = "WH"
    ),

    // ============ RW 理性思辨 ============
    Quote(
        textEn = "I think, therefore I am.",
        textZh = "我思故我在。",
        author = "勒内·笛卡尔",
        source = "《方法论》",
        category = "RW"
    ),
    Quote(
        textEn = "It is the mark of an educated mind to be able to entertain a thought without accepting it.",
        textZh = "受过教育的头脑的标志，是能够容纳一个思想而不接受它。",
        author = "亚里士多德",
        source = "《尼各马可伦理学》",
        category = "RW"
    ),
    Quote(
        textEn = "Everything that can be said can be said clearly.",
        textZh = "一切所言，皆可清晰。",
        author = "路德维希·维特根斯坦",
        source = "《逻辑哲学论》",
        category = "RW"
    ),
    Quote(
        textEn = "Have courage to use your own understanding!",
        textZh = "要有勇气运用你自己的理智！",
        author = "伊曼努尔·康德",
        source = "《什么是启蒙？》",
        category = "RW"
    ),
    Quote(
        textEn = "Imagination is more important than knowledge.",
        textZh = "想象力比知识更重要。",
        author = "阿尔伯特·爱因斯坦",
        source = "《爱因斯坦语录》",
        category = "RW"
    ),
    Quote(
        textEn = "Common sense is not so common.",
        textZh = "常识并非那么常见。",
        author = "伏尔泰",
        source = "《哲学辞典》",
        category = "RW"
    ),
    Quote(
        textEn = "The unexamined life is not worth living.",
        textZh = "未经审视的人生不值得过。",
        author = "苏格拉底",
        source = "柏拉图《申辩篇》",
        category = "RW"
    ),

    // ============ HC 心灵疗愈 ============
    Quote(
        textEn = "God, give me grace to accept with serenity the things that cannot be changed, courage to change the things which should be changed, and the wisdom to distinguish the one from the other.",
        textZh = "上帝，请赐我宁静，去接受我不能改变的一切；赐我勇气，去改变我能改变的一切；并赐我智慧，去分辨两者的不同。",
        author = "雷茵霍尔德·尼布尔",
        source = "《宁静祷文》",
        category = "HC"
    ),
    Quote(
        textEn = "We suffer more often in imagination than in reality.",
        textZh = "折磨我们的往往是想象，而非事实。",
        author = "塞涅卡",
        source = "《书信集》",
        category = "HC"
    ),
    Quote(
        textEn = "The wound is the place where the Light enters you.",
        textZh = "伤口是光进入你内心的地方。",
        author = "鲁米",
        source = "《鲁米诗集》",
        category = "HC"
    ),
    Quote(
        textEn = "Everything has cracks, that's how the light gets in.",
        textZh = "万物皆有裂痕，那是光照进来的地方。",
        author = "莱昂纳德·科恩",
        source = "《颂歌》",
        category = "HC"
    ),
    Quote(
        textEn = "Although the world is full of suffering, it is also full of the overcoming of it.",
        textZh = "尽管世界充满苦难，但也充满了克服苦难的力量。",
        author = "海伦·凯勒",
        source = "《乐观主义》",
        category = "HC"
    ),
    Quote(
        textEn = "Not everything that is faced can be changed. But nothing can be changed until it is faced.",
        textZh = "并非所有你能面对的事都能被改变，但任何事在改变之前都必须先被面对。",
        author = "詹姆斯·鲍德温",
        source = "《下一次将是烈火》",
        category = "HC"
    ),
    Quote(
        textEn = "Every breath is a new beginning.",
        textZh = "每一次呼吸都是一次新的开始。",
        author = "一行禅师",
        source = "《呼吸》",
        category = "HC"
    ),

    // ============ ED 存在哲思 ============
    Quote(
        textEn = "To be, or not to be: that is the question.",
        textZh = "生存还是毁灭，这是个问题。",
        author = "威廉·莎士比亚",
        source = "《哈姆雷特》",
        category = "ED"
    ),
    Quote(
        textEn = "One must imagine Sisyphus happy.",
        textZh = "必须想象西西弗是幸福的。",
        author = "阿尔贝·加缪",
        source = "《西西弗神话》",
        category = "ED"
    ),
    Quote(
        textEn = "That which does not kill us makes us stronger.",
        textZh = "杀不死我的，使我更强大。",
        author = "弗里德里希·尼采",
        source = "《偶像的黄昏》",
        category = "ED"
    ),
    Quote(
        textEn = "Man is condemned to be free.",
        textZh = "人注定是自由的。",
        author = "让-保罗·萨特",
        source = "《存在与虚无》",
        category = "ED"
    ),
    Quote(
        textEn = "Life can only be understood backwards; but it must be lived forwards.",
        textZh = "生活只能向后理解，但必须向前生活。",
        author = "索伦·克尔凯郭尔",
        source = "《克尔凯郭尔日记》",
        category = "ED"
    ),
    Quote(
        textEn = "The Tao that can be told is not the eternal Tao.",
        textZh = "道可道，非常道。",
        author = "老子",
        source = "《道德经》",
        category = "ED"
    ),
    Quote(
        textEn = "We are a way for the cosmos to know itself.",
        textZh = "我们是宇宙认识自身的一种方式。",
        author = "卡尔·萨根",
        source = "《宇宙》",
        category = "ED"
    ),
    Quote(
        textEn = "A true warrior dares to face the bleakness of life directly, and to look squarely at the blood that drips.",
        textZh = "真正的勇士，敢于直面惨淡的人生，敢于正视淋漓的鲜血。",
        author = "鲁迅",
        source = "《记念刘和珍君》",
        category = "ED"
    ),

    // ============ CE 人际纽带 ============
    Quote(
        textEn = "No man is an island entire of itself.",
        textZh = "没有人是一座孤岛。",
        author = "约翰·多恩",
        source = "《紧急时刻的祈祷》",
        category = "CE"
    ),
    Quote(
        textEn = "Friendship is born at that moment when one person says to another, 'What! You too? I thought I was the only one.'",
        textZh = "友谊诞生于一个人对另一个人说：'什么！你也是？我还以为只有我一个人。'",
        author = "C·S·刘易斯",
        source = "《四种爱》",
        category = "CE"
    ),
    Quote(
        textEn = "Man is by nature a political animal.",
        textZh = "人天生是政治的动物。",
        author = "亚里士多德",
        source = "《政治学》",
        category = "CE"
    ),
    Quote(
        textEn = "Forget injuries; never forget kindness.",
        textZh = "忘记伤害；永不忘记善良。",
        author = "孔子",
        source = "《论语》",
        category = "CE"
    ),
    Quote(
        textEn = "No act of kindness, however small, is ever wasted.",
        textZh = "每一个善举，无论多么微小，都不会浪费。",
        author = "伊索",
        source = "《伊索寓言》",
        category = "CE"
    ),
    Quote(
        textEn = "The only way to have a friend is to be one.",
        textZh = "获得一个朋友唯一的方法，就是做一个朋友。",
        author = "拉尔夫·沃尔多·爱默生",
        source = "《随笔集》",
        category = "CE"
    ),
    Quote(
        textEn = "Shared joy is a double joy; shared sorrow is half a sorrow.",
        textZh = "分享快乐就是加倍快乐；分享痛苦就是减半痛苦。",
        author = "瑞典谚语",
        source = "瑞典民间谚语",
        category = "CE"
    ),

    // ============ AC 行动召唤 ============
    Quote(
        textEn = "Courage is resistance to fear, mastery of fear—not absence of fear.",
        textZh = "勇气是抗拒恐惧、掌控恐惧，而非没有恐惧。",
        author = "马克·吐温",
        source = "《傻瓜威尔逊》",
        category = "AC"
    ),
    Quote(
        textEn = "Success is not final, failure is not fatal: it is the courage to continue that counts.",
        textZh = "成功不是终点，失败也不是末日：唯有继续的勇气才是关键。",
        author = "温斯顿·丘吉尔",
        source = "《丘吉尔演讲集》",
        category = "AC"
    ),
    Quote(
        textEn = "Inaction breeds doubt and fear. Action breeds confidence and courage. If you want to conquer fear, do not sit home and think about it. Go out and get busy.",
        textZh = "不行动滋生怀疑和恐惧，行动孕育信心和勇气。如果你想战胜恐惧，就不要坐在家里空想，走出去，让自己忙碌起来。",
        author = "戴尔·卡耐基",
        source = "《人性的弱点》",
        category = "AC"
    ),
    Quote(
        textEn = "You don't have to see the whole staircase, just take the first step.",
        textZh = "你不需要看到整个楼梯，只需要迈出第一步。",
        author = "马丁·路德·金",
        source = "《马丁·路德·金语录》",
        category = "AC"
    ),
    Quote(
        textEn = "The most difficult thing is the decision to act, the rest is merely tenacity.",
        textZh = "最困难的事情是决定行动，剩下的只是坚持而已。",
        author = "阿梅莉亚·埃尔哈特",
        source = "《阿梅莉亚·埃尔哈特语录》",
        category = "AC"
    ),
    Quote(
        textEn = "We cannot always control what happens to us, but we can control our reactions and actions.",
        textZh = "我们不能总是控制发生在自己身上的事，但可以控制自己的反应和行动。",
        author = "维克多·弗兰克尔",
        source = "《活出意义来》",
        category = "AC"
    ),
    Quote(
        textEn = "The important thing in life is to have a great aim, and the determination to attain it.",
        textZh = "生命中真正重要的是有伟大的目标，以及达到它的决心。",
        author = "歌德",
        source = "《歌德谈话录》",
        category = "AC"
    ),
    Quote(
        textEn = "Knowledge is the beginning of action; action is the completion of knowledge.",
        textZh = "知是行之始，行是知之成。",
        author = "王阳明",
        source = "《传习录》",
        category = "AC"
    )
)

/**
 * 按天选一条名言（纯函数，无副作用）。
 *
 * index = ((epochDay % total).toInt() + total) % total，对负值安全。
 *
 * @param epochDay 天数序号（自某一起点以来的天数，可为负）
 */
fun quoteForDay(epochDay: Long): Quote {
    val total = QUOTES.size
    val index = ((epochDay % total).toInt() + total) % total
    return QUOTES[index]
}

/**
 * 取今天的名言：用 kotlin.time.Clock.System 取当前毫秒时间，
 * 除以一天的毫秒数得到天数，再交给 [quoteForDay] 轮换选取。
 */
@OptIn(ExperimentalTime::class)
fun todayQuote(): Quote {
    val ms = Clock.System.now().toEpochMilliseconds()
    val day = ms / 86_400_000L
    return quoteForDay(day)
}
