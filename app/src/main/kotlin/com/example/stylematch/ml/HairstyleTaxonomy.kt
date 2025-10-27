package com.example.stylematch.ml

import com.example.stylematch.ml.FaceAnalyzer.FaceFeature
import com.example.stylematch.ml.FaceAnalyzer.FaceShape
import com.example.stylematch.ml.FaceAnalyzer.Gender

enum class HairLength {
    NONE, VERY_SHORT, EAR_LENGTH, CHIN_LENGTH, SHOULDER_LENGTH, MID_BACK_LENGTH, WAIST_LENGTH
}

enum class HairTexture {
    NONE, STRAIGHT, WAVY, CURLY, COILY
}

enum class HairVolume {
    NONE, LOW, MEDIUM, HIGH
}

enum class HairLayers {
    NONE, LIGHT, MEDIUM, MARKED
}

enum class HairBangs {
    NONE, FULL_BANGS, SIDE_SWEPT, CURTAIN_BANGS, WISPY_BANGS, MICRO_BANGS
}

enum class HairStructure {
    GENERAL_CUT, AFRO, BALD, BEEHIVE, BOB, BOUFFANT, BOWL_CUT, BRAID, BUN, CAESAR, CHONMAGE,
    COMB_OVER, CORNROWS, CREW_CUT, CROP, CROYDON_FACELIFT, CURTAINED_HAIR, DEVILOCK,
    DREADLOCKS, DUCKTAIL, EMO_HAIR, FAUXHAWK, FLATTOP, FRENCH_BRAID, FRENCH_TWIST,
    HI_TOP_FADE, HIME_CUT, HORSESHOE_FLATTOP, INDUCTION_CUT, JIMMY_LIN_HAIRSTYLE,
    LAYERED_HAIR, LIBERTY_SPIKES_HAIR, MEN_POMPADOUR, MEN_WITH_SQUARE_ANGLES, MOHAWK,
    MOP_TOP_HAIR, MULLET, ODANGO_HAIR, PAGEBOY, PERM, PIXIE, PONYTAIL, QUIFF, RATTAIL,
    RAZOR_CUT, RINGLET, SHAG, SIDE_PART_STYLE, SLICKED_BACK_STYLE, SPIKY_HAIR,
    TAPERED_SIDES_STYLE, THE_RACHEL, TONSURE_HAIR, UPDO, WAVE_HAIR_STYLE
}

enum class HairParting {
    NONE_DEFINED, CENTER, SIDE, DEEP_SIDE, ZIG_ZAG
}

data class HairstyleAttributes(
    val length: HairLength,
    val texture: HairTexture,
    val volume: HairVolume,
    val layers: HairLayers,
    val bangs: HairBangs,
    val structure: HairStructure,
    val parting: HairParting,
    val applicableGender: List<Gender> = listOf(Gender.FEMALE, Gender.MALE),
    val suitsFaceShapes: List<FaceShape> = emptyList(),
    val goodForFeatures: List<FaceFeature> = emptyList(),
    val badForFeatures: List<FaceFeature> = emptyList()
)

// Este mapa es la "taxonomía" que el compilador no encuentra.
val hairstyleLabelToAttributesMap: Map<String, HairstyleAttributes> = mapOf(
    "Aaron_Kwok" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.GENERAL_CUT, parting = HairParting.SIDE, applicableGender = listOf(Gender.MALE)),
    "Afro" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.COILY, volume = HairVolume.HIGH, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.AFRO, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Bald" to HairstyleAttributes(length = HairLength.NONE, texture = HairTexture.NONE, volume = HairVolume.NONE, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.BALD, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Beehive" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.HIGH, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.BEEHIVE, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE)),
    "Bob" to HairstyleAttributes(length = HairLength.CHIN_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.BOB, parting = HairParting.SIDE, applicableGender = listOf(Gender.FEMALE)),
    "Bouffant" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.HIGH, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.BOUFFANT, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE)),
    "Bowl_Cut" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.NONE, bangs = HairBangs.FULL_BANGS, structure = HairStructure.BOWL_CUT, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Bun" to HairstyleAttributes(length = HairLength.MID_BACK_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.BUN, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE)),
    "Caesar" to HairstyleAttributes(length = HairLength.VERY_SHORT, texture = HairTexture.STRAIGHT, volume = HairVolume.LOW, layers = HairLayers.LIGHT, bangs = HairBangs.FULL_BANGS, structure = HairStructure.CAESAR, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Chonmage" to HairstyleAttributes(length = HairLength.MID_BACK_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.CHONMAGE, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Comb_Over" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.LOW, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.COMB_OVER, parting = HairParting.SIDE, applicableGender = listOf(Gender.MALE)),
    "Cornrows" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.COILY, volume = HairVolume.LOW, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.CORNROWS, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Crew_Cut" to HairstyleAttributes(length = HairLength.VERY_SHORT, texture = HairTexture.STRAIGHT, volume = HairVolume.LOW, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.CREW_CUT, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Crop" to HairstyleAttributes(length = HairLength.VERY_SHORT, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.MARKED, bangs = HairBangs.FULL_BANGS, structure = HairStructure.CROP, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Croydon_Facelift" to HairstyleAttributes(length = HairLength.MID_BACK_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.LOW, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.CROYDON_FACELIFT, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE)),
    "Curly" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.CURLY, volume = HairVolume.HIGH, layers = HairLayers.MARKED, bangs = HairBangs.NONE, structure = HairStructure.GENERAL_CUT, parting = HairParting.SIDE, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Curly_Hair" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.CURLY, volume = HairVolume.HIGH, layers = HairLayers.MARKED, bangs = HairBangs.NONE, structure = HairStructure.GENERAL_CUT, parting = HairParting.SIDE, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Curtained_Hair" to HairstyleAttributes(length = HairLength.CHIN_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.LIGHT, bangs = HairBangs.CURTAIN_BANGS, structure = HairStructure.CURTAINED_HAIR, parting = HairParting.CENTER, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Cute_Ponytails" to HairstyleAttributes(length = HairLength.MID_BACK_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.PONYTAIL, parting = HairParting.CENTER, applicableGender = listOf(Gender.FEMALE)),
    "Devilock" to HairstyleAttributes(length = HairLength.CHIN_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.NONE, bangs = HairBangs.FULL_BANGS, structure = HairStructure.DEVILOCK, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Dreadlocks" to HairstyleAttributes(length = HairLength.MID_BACK_LENGTH, texture = HairTexture.COILY, volume = HairVolume.HIGH, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.DREADLOCKS, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Ducktail" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.DUCKTAIL, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Emo_hair" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.MARKED, bangs = HairBangs.SIDE_SWEPT, structure = HairStructure.EMO_HAIR, parting = HairParting.DEEP_SIDE, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Fauxhawk" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.FAUXHAWK, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Flattop" to HairstyleAttributes(length = HairLength.VERY_SHORT, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.FLATTOP, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "French_Braid" to HairstyleAttributes(length = HairLength.MID_BACK_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.FRENCH_BRAID, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE)),
    "French_Twist" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.FRENCH_TWIST, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE)),
    "Hi-top_Fade" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.COILY, volume = HairVolume.HIGH, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.HI_TOP_FADE, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Hime_Cut" to HairstyleAttributes(length = HairLength.WAIST_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.NONE, bangs = HairBangs.FULL_BANGS, structure = HairStructure.HIME_CUT, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE)),
    "Horseshoe_Flattop" to HairstyleAttributes(length = HairLength.VERY_SHORT, texture = HairTexture.STRAIGHT, volume = HairVolume.LOW, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.HORSESHOE_FLATTOP, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Induction_Cut" to HairstyleAttributes(length = HairLength.VERY_SHORT, texture = HairTexture.STRAIGHT, volume = HairVolume.LOW, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.INDUCTION_CUT, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Jimmy_Lin_Hairstyle" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.JIMMY_LIN_HAIRSTYLE, parting = HairParting.CENTER, applicableGender = listOf(Gender.MALE)),
    "Layered_Hair" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.WAVY, volume = HairVolume.MEDIUM, layers = HairLayers.MARKED, bangs = HairBangs.NONE, structure = HairStructure.LAYERED_HAIR, parting = HairParting.SIDE, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Liberty_Spikes_Hair" to HairstyleAttributes(length = HairLength.CHIN_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.HIGH, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.LIBERTY_SPIKES_HAIR, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Long_Hair" to HairstyleAttributes(length = HairLength.MID_BACK_LENGTH, texture = HairTexture.WAVY, volume = HairVolume.MEDIUM, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.GENERAL_CUT, parting = HairParting.CENTER, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Medium-Length_Hair" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.GENERAL_CUT, parting = HairParting.SIDE, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Men_Pompadour" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.HIGH, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.MEN_POMPADOUR, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Men_With_Square_Angles" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.MEN_WITH_SQUARE_ANGLES, parting = HairParting.SIDE, applicableGender = listOf(Gender.MALE)),
    "Mohawk" to HairstyleAttributes(length = HairLength.CHIN_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.HIGH, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.MOHAWK, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Mop-Top_Hair" to HairstyleAttributes(length = HairLength.CHIN_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.LIGHT, bangs = HairBangs.FULL_BANGS, structure = HairStructure.MOP_TOP_HAIR, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Mullet" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.MARKED, bangs = HairBangs.NONE, structure = HairStructure.MULLET, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Odango_Hair" to HairstyleAttributes(length = HairLength.MID_BACK_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.ODANGO_HAIR, parting = HairParting.CENTER, applicableGender = listOf(Gender.FEMALE)),
    "Pageboy" to HairstyleAttributes(length = HairLength.CHIN_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.NONE, bangs = HairBangs.FULL_BANGS, structure = HairStructure.PAGEBOY, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE)),
    "Perm" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.CURLY, volume = HairVolume.HIGH, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.PERM, parting = HairParting.SIDE, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Pixie_Cut" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.MARKED, bangs = HairBangs.SIDE_SWEPT, structure = HairStructure.PIXIE, parting = HairParting.SIDE, applicableGender = listOf(Gender.FEMALE)),
    "Ponytail" to HairstyleAttributes(length = HairLength.MID_BACK_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.PONYTAIL, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE)),
    "Quiff" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.HIGH, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.QUIFF, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Rattail" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.LOW, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.RATTAIL, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Razor_Cut" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.MARKED, bangs = HairBangs.SIDE_SWEPT, structure = HairStructure.RAZOR_CUT, parting = HairParting.SIDE, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Ringlet" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.CURLY, volume = HairVolume.HIGH, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.RINGLET, parting = HairParting.SIDE, applicableGender = listOf(Gender.FEMALE)),
    "Shag" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.WAVY, volume = HairVolume.HIGH, layers = HairLayers.MARKED, bangs = HairBangs.CURTAIN_BANGS, structure = HairStructure.SHAG, parting = HairParting.CENTER, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Shoulder-Length_Hair" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.WAVY, volume = HairVolume.MEDIUM, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.GENERAL_CUT, parting = HairParting.SIDE, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Side_Part" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.SIDE_PART_STYLE, parting = HairParting.SIDE, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Slicked-back" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.LOW, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.SLICKED_BACK_STYLE, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Spiky_Hair" to HairstyleAttributes(length = HairLength.VERY_SHORT, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.MARKED, bangs = HairBangs.NONE, structure = HairStructure.SPIKY_HAIR, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Tapered_Sides" to HairstyleAttributes(length = HairLength.VERY_SHORT, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.TAPERED_SIDES_STYLE, parting = HairParting.SIDE, applicableGender = listOf(Gender.MALE)),
    "The_Rachel" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.HIGH, layers = HairLayers.MARKED, bangs = HairBangs.SIDE_SWEPT, structure = HairStructure.THE_RACHEL, parting = HairParting.SIDE, applicableGender = listOf(Gender.FEMALE)),
    "Tonsure_Hair" to HairstyleAttributes(length = HairLength.EAR_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.LOW, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.TONSURE_HAIR, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.MALE)),
    "Updo" to HairstyleAttributes(length = HairLength.MID_BACK_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.UPDO, parting = HairParting.NONE_DEFINED, applicableGender = listOf(Gender.FEMALE)),
    "Waist-Length_Hair" to HairstyleAttributes(length = HairLength.WAIST_LENGTH, texture = HairTexture.STRAIGHT, volume = HairVolume.MEDIUM, layers = HairLayers.NONE, bangs = HairBangs.NONE, structure = HairStructure.GENERAL_CUT, parting = HairParting.CENTER, applicableGender = listOf(Gender.FEMALE, Gender.MALE)),
    "Wave_Hair" to HairstyleAttributes(length = HairLength.SHOULDER_LENGTH, texture = HairTexture.WAVY, volume = HairVolume.MEDIUM, layers = HairLayers.LIGHT, bangs = HairBangs.NONE, structure = HairStructure.WAVE_HAIR_STYLE, parting = HairParting.SIDE, applicableGender = listOf(Gender.FEMALE, Gender.MALE))
)