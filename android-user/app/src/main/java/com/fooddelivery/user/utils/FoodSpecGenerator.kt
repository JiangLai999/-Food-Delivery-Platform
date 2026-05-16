package com.fooddelivery.user.utils

import com.fooddelivery.user.model.FoodItem
import java.math.BigDecimal

/**
 * 餐品规格智能生成器
 * 根据餐品名称和类型自动生成适合的规格、口味、配料选项
 */
object FoodSpecGenerator {

    /**
     * 餐品分类
     */
    enum class FoodCategory {
        COFFEE_TEA,      // 咖啡茶饮
        NOODLE_RICE,    // 面饭类
        SNACKS,         // 小吃炸物
        DESSERT,        // 甜点蛋糕
        DRINKS,         // 饮料水
        FRESH_FOOD,     // 生鲜果蔬
        FAST_FOOD,      // 快餐便利
        DEFAULT          // 默认
    }

    /**
     * 规格组
     */
    data class SpecGroup(
        val id: Long,
        val name: String,
        val required: Boolean,
        val options: List<SpecOption>
    )

    /**
     * 规格选项
     */
    data class SpecOption(
        val id: Long,
        val name: String,
        val price: BigDecimal,
        val description: String = ""
    )

    /**
     * 餐品形容词
     */
    data class FoodDescription(
        val shortDesc: String,      // 短描述
        val longDesc: String,       // 长描述
        val tasteDesc: String,      // 口味描述
        val highlight: List<String>  // 亮点标签
    )

    /**
     * 根据餐品名称判断分类
     */
    fun getFoodCategory(foodName: String): FoodCategory {
        val name = foodName.lowercase()
        
        return when {
            // 咖啡茶饮
            name.contains("咖啡") || name.contains("拿铁") || name.contains("奶茶") || 
            name.contains("茶") || name.contains("果茶") || name.contains("柠檬") ||
            name.contains("葡萄") || name.contains("莓莓") || name.contains("甘露") ||
            name.contains("养乐多") || name.contains("波霸") || name.contains("珍珠") -> {
                FoodCategory.COFFEE_TEA
            }
            // 面饭类
            name.contains("面") || name.contains("饭") || name.contains("炒饭") || 
            name.contains("拌饭") || name.contains("盖饭") || name.contains("卤肉饭") ||
            name.contains("叉烧") || name.contains("炸酱") || name.contains("担担") -> {
                FoodCategory.NOODLE_RICE
            }
            // 小吃炸物
            name.contains("炸") || name.contains("薯条") || name.contains("鸡翅") ||
            name.contains("鸡块") || name.contains("汉堡") || name.contains("披萨") ||
            name.contains("炸鸡") || name.contains("烤串") || name.contains("丸子") -> {
                FoodCategory.SNACKS
            }
            // 甜点蛋糕
            name.contains("蛋糕") || name.contains("甜点") || name.contains("慕斯") ||
            name.contains("提拉米苏") || name.contains("芝士") || name.contains("奶油") ||
            name.contains("冰淇淋") || name.contains("布丁") || name.contains("千层") -> {
                FoodCategory.DESSERT
            }
            // 饮料水
            name.contains("水") || name.contains("可乐") || name.contains("果汁") ||
            name.contains("酸奶") || name.contains("牛奶") || name.contains("椰子") -> {
                FoodCategory.DRINKS
            }
            // 生鲜果蔬
            name.contains("水果") || name.contains("苹果") || name.contains("香蕉") ||
            name.contains("橙子") || name.contains("蓝莓") || name.contains("草莓") ||
            name.contains("车厘子") || name.contains("牛油果") || name.contains("青菜") ||
            name.contains("蔬菜") || name.contains("鸡蛋") || name.contains("虾") ||
            name.contains("鱼") || name.contains("肉") -> {
                FoodCategory.FRESH_FOOD
            }
            // 快餐便利
            name.contains("便当") || name.contains("三明治") || name.contains("沙拉") ||
            name.contains("泡面") || name.contains("方便面") || name.contains("饼干") ||
            name.contains("零食") -> {
                FoodCategory.FAST_FOOD
            }
            else -> FoodCategory.DEFAULT
        }
    }

    /**
     * 生成规格组
     */
    fun generateSpecGroups(foodItem: FoodItem): List<SpecGroup> {
        val foodName = foodItem.name ?: ""
        val category = getFoodCategory(foodName)
        val price = foodItem.price ?: BigDecimal("0")
        
        return when (category) {
            FoodCategory.COFFEE_TEA -> generateCoffeeTeaSpecs(price)
            FoodCategory.NOODLE_RICE -> generateNoodleRiceSpecs(price)
            FoodCategory.SNACKS -> generateSnackSpecs(price)
            FoodCategory.DESSERT -> generateDessertSpecs(price)
            FoodCategory.DRINKS -> generateDrinkSpecs(price)
            FoodCategory.FRESH_FOOD -> generateFreshFoodSpecs(price)
            FoodCategory.FAST_FOOD -> generateFastFoodSpecs(price)
            FoodCategory.DEFAULT -> generateDefaultSpecs(price)
        }
    }

    // 咖啡茶饮规格
    private fun generateCoffeeTeaSpecs(price: BigDecimal): List<SpecGroup> = listOf(
        SpecGroup(
            id = 1,
            name = "温度",
            required = true,
            options = listOf(
                SpecOption(1, "去冰", BigDecimal("0"), "口感更浓郁"),
                SpecOption(2, "少冰", BigDecimal("0"), "清爽与浓郁兼顾"),
                SpecOption(3, "正常冰", BigDecimal("0"), "标准口味"),
                SpecOption(4, "热饮", BigDecimal("0"), "温暖暖胃")
            )
        ),
        SpecGroup(
            id = 2,
            name = "甜度",
            required = true,
            options = listOf(
                SpecOption(5, "无糖", BigDecimal("0"), "纯粹原味"),
                SpecOption(6, "少糖", BigDecimal("0"), "微甜不腻"),
                SpecOption(7, "标准糖", BigDecimal("0"), "完美甜度"),
                SpecOption(8, "多糖", BigDecimal("0"), "甜蜜享受")
            )
        ),
        SpecGroup(
            id = 3,
            name = "加料",
            required = false,
            options = listOf(
                SpecOption(9, "加珍珠", BigDecimal("3"), "Q弹珍珠"),
                SpecOption(10, "加波霸", BigDecimal("3"), "大颗波霸"),
                SpecOption(11, "加椰果", BigDecimal("2"), "清爽椰果"),
                SpecOption(12, "加芝士", BigDecimal("5"), "浓郁芝士"),
                SpecOption(13, "加燕麦", BigDecimal("4"), "燕麦奶香")
            )
        )
    )

    // 面饭类规格
    private fun generateNoodleRiceSpecs(price: BigDecimal): List<SpecGroup> = listOf(
        SpecGroup(
            id = 1,
            name = "份量",
            required = true,
            options = listOf(
                SpecOption(1, "小份", price.multiply(BigDecimal("0.8")), "适合胃口小的朋友"),
                SpecOption(2, "标准", BigDecimal("0"), "适中份量"),
                SpecOption(3, "大份", price.multiply(BigDecimal("1.3")), "大胃王首选")
            )
        ),
        SpecGroup(
            id = 2,
            name = "辣度",
            required = false,
            options = listOf(
                SpecOption(4, "不辣", BigDecimal("0"), "原汁原味"),
                SpecOption(5, "微辣", BigDecimal("0"), "轻微辣感"),
                SpecOption(6, "中辣", BigDecimal("0"), "适中辣度"),
                SpecOption(7, "特辣", BigDecimal("0"), "辣到过瘾"),
                SpecOption(8, "麻辣", BigDecimal("2"), "麻辣鲜香")
            )
        ),
        SpecGroup(
            id = 3,
            name = "配料",
            required = false,
            options = listOf(
                SpecOption(9, "加煎蛋", BigDecimal("3"), "流心煎蛋"),
                SpecOption(10, "加卤蛋", BigDecimal("4"), "入味卤蛋"),
                SpecOption(11, "加肉", BigDecimal("8"), "肉感满足"),
                SpecOption(12, "加青笋", BigDecimal("3"), "清爽解腻"),
                SpecOption(13, "加豆芽", BigDecimal("2"), "脆爽口感")
            )
        )
    )

    // 小吃炸物规格
    private fun generateSnackSpecs(price: BigDecimal): List<SpecGroup> = listOf(
        SpecGroup(
            id = 1,
            name = "口味",
            required = true,
            options = listOf(
                SpecOption(1, "原味", BigDecimal("0"), "经典原味"),
                SpecOption(2, "番茄味", BigDecimal("0"), "酸甜可口"),
                SpecOption(3, "烧烤味", BigDecimal("0"), "浓郁烟熏"),
                SpecOption(4, "蜂蜜芥末", BigDecimal("0"), "甜辣适中"),
                SpecOption(5, "辣味", BigDecimal("0"), "香辣过瘾")
            )
        ),
        SpecGroup(
            id = 2,
            name = "份量",
            required = false,
            options = listOf(
                SpecOption(6, "小份", price.multiply(BigDecimal("0.7")), "尝鲜首选"),
                SpecOption(7, "中份", BigDecimal("0"), "适中分享"),
                SpecOption(8, "大份", price.multiply(BigDecimal("1.5")), "超值大份")
            )
        ),
        SpecGroup(
            id = 3,
            name = "蘸酱",
            required = false,
            options = listOf(
                SpecOption(9, "番茄酱", BigDecimal("0"), "经典蘸酱"),
                SpecOption(10, "沙拉酱", BigDecimal("0"), "清爽口感"),
                SpecOption(11, "辣椒粉", BigDecimal("0"), "干碟更香"),
                SpecOption(12, "蜂蜜酱", BigDecimal("2"), "甜蜜风味")
            )
        )
    )

    // 甜点规格
    private fun generateDessertSpecs(price: BigDecimal): List<SpecGroup> = listOf(
        SpecGroup(
            id = 1,
            name = "规格",
            required = true,
            options = listOf(
                SpecOption(1, "小块", price.multiply(BigDecimal("0.6")), "浅尝辄止"),
                SpecOption(2, "标准块", BigDecimal("0"), "恰到好处"),
                SpecOption(3, "大块", price.multiply(BigDecimal("1.4")), "满足享受")
            )
        ),
        SpecGroup(
            id = 2,
            name = "甜度",
            required = false,
            options = listOf(
                SpecOption(4, "无糖", BigDecimal("0"), "健康无负担"),
                SpecOption(5, "少糖", BigDecimal("0"), "微甜适中"),
                SpecOption(6, "标准甜", BigDecimal("0"), "正常甜度")
            )
        ),
        SpecGroup(
            id = 3,
            name = "配料",
            required = false,
            options = listOf(
                SpecOption(7, "加水果", BigDecimal("5"), "新鲜水果"),
                SpecOption(8, "加坚果", BigDecimal("4"), "香脆坚果"),
                SpecOption(9, "加奶油", BigDecimal("3"), "双重浓郁"),
                SpecOption(10, "加巧克力", BigDecimal("4"), "甜蜜升级")
            )
        )
    )

    // 饮料规格
    private fun generateDrinkSpecs(price: BigDecimal): List<SpecGroup> = listOf(
        SpecGroup(
            id = 1,
            name = "容量",
            required = true,
            options = listOf(
                SpecOption(1, "330ml", price.multiply(BigDecimal("0.7")), "小瓶装"),
                SpecOption(2, "500ml", BigDecimal("0"), "标准装"),
                SpecOption(3, "550ml", BigDecimal("0"), "常规瓶"),
                SpecOption(4, "1L", price.multiply(BigDecimal("1.5")), "大瓶装")
            )
        ),
        SpecGroup(
            id = 2,
            name = "数量",
            required = true,
            options = listOf(
                SpecOption(5, "1瓶", BigDecimal("0"), "单瓶"),
                SpecOption(6, "6瓶装", price.multiply(BigDecimal("5")), "超值优惠"),
                SpecOption(7, "12瓶装", price.multiply(BigDecimal("9")), "箱装特惠"),
                SpecOption(8, "24瓶装", price.multiply(BigDecimal("16")), "整箱囤货")
            )
        )
    )

    // 生鲜规格
    private fun generateFreshFoodSpecs(price: BigDecimal): List<SpecGroup> = listOf(
        SpecGroup(
            id = 1,
            name = "规格",
            required = true,
            options = listOf(
                SpecOption(1, "小份", price.multiply(BigDecimal("0.6")), "1-2人食"),
                SpecOption(2, "中份", BigDecimal("0"), "2-3人食"),
                SpecOption(3, "大份", price.multiply(BigDecimal("1.5")), "家庭装")
            )
        ),
        SpecGroup(
            id = 2,
            name = "处理",
            required = false,
            options = listOf(
                SpecOption(4, "净菜", BigDecimal("0"), "已清洗"),
                SpecOption(5, "半成品", BigDecimal("2"), "已切配"),
                SpecOption(6, "原生态", BigDecimal("0"), "新鲜原味")
            )
        )
    )

    // 快餐规格
    private fun generateFastFoodSpecs(price: BigDecimal): List<SpecGroup> = listOf(
        SpecGroup(
            id = 1,
            name = "组合",
            required = true,
            options = listOf(
                SpecOption(1, "单点", BigDecimal("0"), "自由搭配"),
                SpecOption(2, "套餐A", price.multiply(BigDecimal("1.2")), "经典套餐"),
                SpecOption(3, "套餐B", price.multiply(BigDecimal("1.4")), "豪华套餐")
            )
        ),
        SpecGroup(
            id = 2,
            name = "加料",
            required = false,
            options = listOf(
                SpecOption(4, "加蛋", BigDecimal("3"), "营养加料"),
                SpecOption(5, "加肉", BigDecimal("8"), "肉食主义"),
                SpecOption(6, "加蔬菜", BigDecimal("3"), "均衡营养")
            )
        )
    )

    // 默认规格
    private fun generateDefaultSpecs(price: BigDecimal): List<SpecGroup> = listOf(
        SpecGroup(
            id = 1,
            name = "规格",
            required = true,
            options = listOf(
                SpecOption(1, "标准", BigDecimal("0"), "标准份量")
            )
        )
    )

    /**
     * 生成餐品描述
     */
    fun generateFoodDescription(foodItem: FoodItem): FoodDescription {
        val foodName = foodItem.name ?: ""
        val category = getFoodCategory(foodName)
        val baseDesc = foodItem.description ?: ""
        
        return when (category) {
            FoodCategory.COFFEE_TEA -> FoodDescription(
                shortDesc = getCoffeeTeaShortDesc(foodName),
                longDesc = getCoffeeTeaLongDesc(foodName, baseDesc),
                tasteDesc = "现萃茶底搭配新鲜物料，口感层次丰富，回甘悠长",
                highlight = listOf("现萃工艺", "新鲜物料", "手工调制")
            )
            FoodCategory.NOODLE_RICE -> FoodDescription(
                shortDesc = getNoodleRiceShortDesc(foodName),
                longDesc = getNoodleRiceLongDesc(foodName, baseDesc),
                tasteDesc = "手工制作，面条劲道，汤底浓郁，回味无穷",
                highlight = listOf("手工制作", "地道风味", "现做热乎")
            )
            FoodCategory.SNACKS -> FoodDescription(
                shortDesc = getSnackShortDesc(foodName),
                longDesc = getSnackLongDesc(foodName, baseDesc),
                tasteDesc = "外酥里嫩，香气扑鼻，一口满足",
                highlight = listOf("现炸出锅", "酥脆可口", "香气四溢")
            )
            FoodCategory.DESSERT -> FoodDescription(
                shortDesc = getDessertShortDesc(foodName),
                longDesc = getDessertLongDesc(foodName, baseDesc),
                tasteDesc = "绵密细腻，入口即化，甜而不腻",
                highlight = listOf("动物奶油", "当日新鲜", "手工烘焙")
            )
            FoodCategory.DRINKS -> FoodDescription(
                shortDesc = getDrinkShortDesc(foodName),
                longDesc = getDrinkLongDesc(foodName, baseDesc),
                tasteDesc = "清爽解渴，冰凉舒心",
                highlight = listOf("正品保证", "新鲜日期", "畅饮首选")
            )
            FoodCategory.FRESH_FOOD -> FoodDescription(
                shortDesc = getFreshShortDesc(foodName),
                longDesc = getFreshLongDesc(foodName, baseDesc),
                tasteDesc = "新鲜直采，品质保证",
                highlight = listOf("新鲜直达", "精挑细选", "品质保证")
            )
            FoodCategory.FAST_FOOD -> FoodDescription(
                shortDesc = getFastFoodShortDesc(foodName),
                longDesc = getFastFoodLongDesc(foodName, baseDesc),
                tasteDesc = "方便快捷，美味不减",
                highlight = listOf("即食便利", "美味便捷", "果腹之选")
            )
            FoodCategory.DEFAULT -> FoodDescription(
                shortDesc = baseDesc,
                longDesc = baseDesc,
                tasteDesc = "美味可口",
                highlight = listOf("精选食材", "用心烹饪")
            )
        }
    }

    // 各类别描述生成
    private fun getCoffeeTeaShortDesc(name: String) = when {
        name.contains("拿铁") -> "丝绒口感，奶香浓郁"
        name.contains("奶茶") -> "茶香浓郁，奶味醇厚"
        name.contains("果茶") -> "清爽果香，茶韵回甘"
        name.contains("柠檬") -> "清新解腻，酸爽开胃"
        name.contains("葡萄") -> "手剥葡萄，果肉饱满"
        name.contains("莓莓") -> "莓果Fresh，酸甜可口"
        name.contains("甘露") -> "芒果西柚，经典港式"
        else -> "现萃茶饮，手工调制"
    }

    private fun getCoffeeTeaLongDesc(name: String, base: String) = when {
        name.contains("拿铁") -> "精选阿拉比卡咖啡豆，搭配进口鲜奶，打发细腻奶泡，口感丝滑绵密，咖啡香气与奶香完美融合"
        name.contains("生椰") -> "选用新鲜椰肉现榨椰浆，椰香浓郁自然，与咖啡完美融合，冷热皆宜的明星产品"
        name.contains("多肉") -> "手工剥取新鲜葡萄果肉，搭配清香茶底，每一口都能吃到真实果粒，清爽不腻"
        else -> base.ifEmpty { "精选优质茶叶，新鲜萃取，保留茶叶原始香气，口感清新自然" }
    }

    private fun getNoodleRiceShortDesc(name: String) = when {
        name.contains("炸酱") -> "老北京传统风味"
        name.contains("担担") -> "麻辣鲜香川味"
        name.contains("刀削") -> "手工刀削劲道"
        name.contains("炒饭") -> "粒粒分明锅气足"
        else -> "地道风味，现做热乎"
    }

    private fun getNoodleRiceLongDesc(name: String, base: String) = when {
        name.contains("炸酱") -> "采用五花肉制成的炸酱，肉丁肥瘦相间，酱香浓郁，面条劲道有嚼劲，配上黄瓜丝、豆芽菜，色香味俱全"
        name.contains("麻婆") -> "精选嫩豆腐，牛肉末炒制臊子，麻辣鲜香，豆腐嫩滑，麻辣感层次分明，开胃下饭神器"
        name.contains("水煮") -> "活鱼现杀，鱼片薄如蝉翼，麻辣汤底浓郁醇厚，鱼肉鲜嫩爽滑，配菜丰富"
        else -> base.ifEmpty { "面条劲道爽滑，汤底浓郁鲜美，配料丰富新鲜，现点现做，热气腾腾" }
    }

    private fun getSnackShortDesc(name: String) = when {
        name.contains("薯条") -> "外酥里软，薯香浓郁"
        name.contains("鸡翅") -> "皮脆肉嫩，多汁入味"
        name.contains("炸鸡") -> "金黄酥脆，鸡肉嫩滑"
        name.contains("汉堡") -> "肉饼鲜嫩多汁"
        else -> "香脆可口，回味无穷"
    }

    private fun getSnackLongDesc(name: String, base: String) = when {
        name.contains("薯条") -> "选用新鲜土豆切成均匀条状，油温精准控制，炸至金黄酥脆，外酥里软，撒上细盐更添风味"
        name.contains("翅") -> "精选鸡中翅，腌制12小时以上入味，秘制配方炸至外皮酥脆，鸡肉鲜嫩多汁，咬一口满嘴香"
        else -> base.ifEmpty { "精选食材，现炸出锅，外酥里嫩，香气扑鼻，是解馋的最佳选择" }
    }

    private fun getDessertShortDesc(name: String) = when {
        name.contains("提拉米苏") -> "意式经典，层层叠加"
        name.contains("奶油") -> "动物奶油，入口即化"
        name.contains("慕斯") -> "细腻绵密，清爽不腻"
        name.contains("芝士") -> "浓郁奶香，绵密口感"
        else -> "甜蜜滋味，幸福味道"
    }

    private fun getDessertLongDesc(name: String, base: String) = when {
        name.contains("提拉米苏") -> "经典意式甜品，手指饼干蘸满咖啡酒，手指饼干与马斯卡彭奶酪层层叠加，可可粉微苦回甜，口感丰富"
        name.contains("草莓") -> "精选当季新鲜草莓，动物奶油打发，草莓颗粒清晰可见，酸甜平衡，少女心爆棚"
        name.contains("千层") -> "手工煎制班戟皮，层层叠加，奶油馅料充足，每一口都是满足"
        else -> base.ifEmpty { "精选优质原料，当日新鲜制作，动物奶油天然健康，口感细腻绵密，甜而不腻" }
    }

    private fun getDrinkShortDesc(name: String) = when {
        name.contains("水") -> "纯净水源，清甜可口"
        name.contains("可乐") -> "冰爽刺激，畅快解渴"
        name.contains("果汁") -> "新鲜水果，现榨甘甜"
        name.contains("椰子") -> "天然椰香，清凉解渴"
        else -> "清爽解渴必备"
    }

    private fun getDrinkLongDesc(name: String, base: String) = when {
        name.contains("农夫山泉") -> "天然矿泉水，水源优质，口感清甜，适合日常饮用"
        name.contains("可乐") -> "经典碳酸饮料，冰镇后饮用更佳，气泡充足，畅爽解渴"
        name.contains("椰子") -> "新鲜椰子水，天然清甜，富含电解质，美容养颜"
        else -> base.ifEmpty { "正品保证，新鲜日期，性价比高，是日常饮用的首选" }
    }

    private fun getFreshShortDesc(name: String) = when {
        name.contains("青菜") -> "新鲜蔬菜翠绿欲滴"
        name.contains("鸡蛋") -> "农家散养，营养丰富"
        name.contains("虾") -> "鲜活海捕，个大肉嫩"
        name.contains("车厘子") -> "进口JJ级，颗颗饱满"
        else -> "新鲜直达，品质保证"
    }

    private fun getFreshLongDesc(name: String, base: String) = when {
        name.contains("鸡蛋") -> "农家散养鸡蛋，营养价值高，蛋黄饱满，颜色金黄，蛋白质含量丰富"
        name.contains("虾") -> "新鲜海捕，活力十足，肉质Q弹，配料新鲜"
        name.contains("车厘子") -> "智利进口，JJ级大果，颗颗饱满有光泽，甜度高水分足"
        name.contains("牛油果") -> "墨西哥进口，自然成熟，口感绵密，营养丰富"
        else -> base.ifEmpty { "新鲜直达，精挑细选，品质保证，冷链配送" }
    }

    private fun getFastFoodShortDesc(name: String) = when {
        name.contains("便当") -> "日式风味，搭配丰富"
        name.contains("三明治") -> "新鲜面包，馅料丰富"
        name.contains("沙拉") -> "新鲜蔬菜，低卡健康"
        else -> "方便快捷，美味依旧"
    }

    private fun getFastFoodLongDesc(name: String, base: String) = when {
        name.contains("便当") -> "日式精致便当，主食+配菜+汤品营养均衡，菜品新鲜制作，口味地道"
        name.contains("三明治") -> "新鲜烘焙面包，夹入丰富馅料，营养均衡，是忙碌生活中的快捷美味"
        name.contains("泡面") -> "经典口味，量大实惠，是深夜加班、追剧的最佳伴侣"
        else -> base.ifEmpty { "方便快捷美味，满足你的即时需求，是解馋饱腹的好选择" }
    }
}
