package com.hashcaller.app.view.ui.sms.individual.util

import android.graphics.Color
import android.os.Build
import android.os.Looper
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.*

val SPAMMER_TYPE_BUSINESS = 1
val SPAMMER_TYPE_PEERSON= 2
val SPAMMER_TYPE_SALES = 3
val SPAMMER_TYPE_SCAM = 4
val SPAMMER_TYPE_NOT_SPECIFIC = 5





const val INFO_FOUND_IN_SERVER = 1
const val INFO_NOT_FOUND_IN_SERVER = 0
const val SEARCHING_FOR_INFO = 2

const val KEY_INTENT_BLOCK_LIST = "numberContaining"
const val NUMBER_STARTS_WITH= 0
const val NUMBER_CONTAINING = 1
const val NUMBER_ENDS_WITH = 2
const val EXACT_NUMBER = 3

const val SET_DEF_SMS_REQ_CODE = 222
const val UNMARK_ITEM = 0
const val MARK_ITEM = 1
const val EXPAND_LAYOUT = 2 // EXPAND EXPANDABLE LAYOUT
const val COMPRESS_LAYOUT = 3


const val TYPE_CLICK = 0
const val TYPE_LONG_PRESS = 1
const val TYPE_MAKE_CALL= 2
const val TYPE_CLICK_VIWE_INDIVIDUAL_CONTACT = 3
const val TYPE_CLICK_VIEW_CALL_HISTORY = 4
const val TYPE_CLICK_SCREENING_ROLE = 5
const val TYPE_CLICK_DISMISS_SCREENING_ROLE = 6
const val TYPE_CLICK_ALLOW_OVERLAY = 7
const val TYPE_CLICK_DISMISS_OVERLAY = 8

const val SMS_NOT_READ = 0
const val SMS_READED = 1

const val BLOCK_INCOMMING_CALL = 1


const val SHARED_PREF_BLOCK_CONFIGURATIONS = "blockTopSpammers"
const val IS_CALL_BLOCK_NOTIFICATION_ENABLED = "isCallblkntfnEnabled"
const val IS_SMS_BLOCK_NOTIFICATION_ENABLED = "isSMSblkntfnEnabled"
const val SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS = "notifcnConfig"
const val APP_NAME = "app_name"
const val APP_LICENSES = "app_licenses"
const val APP_FAQ = "app_faq"
const val APP_VERSION_NAME = "app_version_name"
const val APP_ICON_IDS = "app_icon_ids"
const val APP_ID = "app_id"
const val APP_LAUNCHER_NAME = "app_launcher_name"
const val REAL_FILE_PATH = "real_file_path_2"
const val IS_FROM_GALLERY = "is_from_gallery"
const val BROADCAST_REFRESH_MEDIA = "com.simplemobiletools.REFRESH_MEDIA"
const val REFRESH_PATH = "refresh_path"
const val IS_CUSTOMIZING_COLORS = "is_customizing_colors"
const val NOMEDIA = ".nomedia"
const val ALARM_SOUND_TYPE_ALARM = 1
const val ALARM_SOUND_TYPE_NOTIFICATION = 2
const val YOUR_ALARM_SOUNDS_MIN_ID = 1000
const val SHOW_FAQ_BEFORE_MAIL = "show_faq_before_mail"
const val INVALID_NAVIGATION_BAR_COLOR = -1
const val CHOPPED_LIST_DEFAULT_SIZE = 50
const val SAVE_DISCARD_PROMPT_INTERVAL = 1000L
val DEFAULT_WIDGET_BG_COLOR = Color.parseColor("#AA000000")
const val SD_OTG_PATTERN = "^/storage/[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"
const val SD_OTG_SHORT = "^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"
const val KEY_PHONE = "phone"
const val CONTACT_ID = "contact_id"
const val IS_PRIVATE = "is_private"
const val MD5 = "MD5"

const val IS_MUTED_ADDRESS = 0
const val IS_NOT_MUTED_ADDRESS = 1
const val IS_BLOCKED_ADDRESS = 2

const val OPERTION_MUTE = 0
const val OPERTION_DELETE = 1

//shared preferences
const val PREFS_KEY = "Prefs"
const val APP_RUN_COUNT = "app_run_count"
const val LAST_VERSION = "last_version"
const val TREE_URI = "tree_uri_2"
const val OTG_TREE_URI = "otg_tree_uri_2"
const val SD_CARD_PATH = "sd_card_path_2"
const val OTG_REAL_PATH = "otg_real_path_2"
const val INTERNAL_STORAGE_PATH = "internal_storage_path"
const val TEXT_COLOR = "text_color"
const val BACKGROUND_COLOR = "background_color"
const val PRIMARY_COLOR = "primary_color_2"
const val APP_ICON_COLOR = "app_icon_color"
const val NAVIGATION_BAR_COLOR = "navigation_bar_color"
const val DEFAULT_NAVIGATION_BAR_COLOR = "default_navigation_bar_color"
const val LAST_HANDLED_SHORTCUT_COLOR = "last_handled_shortcut_color"
const val LAST_ICON_COLOR = "last_icon_color"
const val CUSTOM_TEXT_COLOR = "custom_text_color"
const val CUSTOM_BACKGROUND_COLOR = "custom_background_color"
const val CUSTOM_PRIMARY_COLOR = "custom_primary_color"
const val CUSTOM_NAVIGATION_BAR_COLOR = "custom_navigation_bar_color"
const val CUSTOM_APP_ICON_COLOR = "custom_app_icon_color"
const val WIDGET_BG_COLOR = "widget_bg_color"
const val WIDGET_TEXT_COLOR = "widget_text_color"
const val PASSWORD_PROTECTION = "password_protection"
const val PASSWORD_HASH = "password_hash"
const val PROTECTION_TYPE = "protection_type"
const val APP_PASSWORD_PROTECTION = "app_password_protection"
const val APP_PASSWORD_HASH = "app_password_hash"
const val APP_PROTECTION_TYPE = "app_protection_type"
const val DELETE_PASSWORD_PROTECTION = "delete_password_protection"
const val DELETE_PASSWORD_HASH = "delete_password_hash"
const val DELETE_PROTECTION_TYPE = "delete_protection_type"
const val PROTECTED_FOLDER_PATH = "protected_folder_path_"
const val PROTECTED_FOLDER_HASH = "protected_folder_hash_"
const val PROTECTED_FOLDER_TYPE = "protected_folder_type_"
const val KEEP_LAST_MODIFIED = "keep_last_modified"
const val USE_ENGLISH = "use_english"
const val WAS_USE_ENGLISH_TOGGLED = "was_use_english_toggled"
const val WAS_SHARED_THEME_EVER_ACTIVATED = "was_shared_theme_ever_activated"
const val IS_USING_SHARED_THEME = "is_using_shared_theme"
const val SHOULD_USE_SHARED_THEME = "should_use_shared_theme"
const val WAS_SHARED_THEME_FORCED = "was_shared_theme_forced"
const val WAS_CUSTOM_THEME_SWITCH_DESCRIPTION_SHOWN = "was_custom_theme_switch_description_shown"
const val SHOW_INFO_BUBBLE = "show_info_bubble"
const val LAST_CONFLICT_RESOLUTION = "last_conflict_resolution"
const val LAST_CONFLICT_APPLY_TO_ALL = "last_conflict_apply_to_all"
const val HAD_THANK_YOU_INSTALLED = "had_thank_you_installed"
const val SKIP_DELETE_CONFIRMATION = "skip_delete_confirmation"
const val ENABLE_PULL_TO_REFRESH = "enable_pull_to_refresh"
const val SCROLL_HORIZONTALLY = "scroll_horizontally"
const val PREVENT_PHONE_FROM_SLEEPING = "prevent_phone_from_sleeping"
const val LAST_USED_VIEW_PAGER_PAGE = "last_used_view_pager_page"
const val USE_24_HOUR_FORMAT = "use_24_hour_format"
const val SUNDAY_FIRST = "sunday_first"
const val WAS_ALARM_WARNING_SHOWN = "was_alarm_warning_shown"
const val WAS_REMINDER_WARNING_SHOWN = "was_reminder_warning_shown"
const val USE_SAME_SNOOZE = "use_same_snooze"
const val SNOOZE_TIME = "snooze_delay"
const val VIBRATE_ON_BUTTON_PRESS = "vibrate_on_button_press"
const val YOUR_ALARM_SOUNDS = "your_alarm_sounds"
const val SILENT = "silent"
const val OTG_PARTITION = "otg_partition_2"
const val IS_USING_MODIFIED_APP_ICON = "is_using_modified_app_icon"
const val INITIAL_WIDGET_HEIGHT = "initial_widget_height"
const val WIDGET_ID_TO_MEASURE = "widget_id_to_measure"
const val WAS_ORANGE_ICON_CHECKED = "was_orange_icon_checked"
const val WAS_APP_ON_SD_SHOWN = "was_app_on_sd_shown"
const val WAS_BEFORE_ASKING_SHOWN = "was_before_asking_shown"
const val WAS_BEFORE_RATE_SHOWN = "was_before_rate_shown"
const val WAS_INITIAL_UPGRADE_TO_PRO_SHOWN = "was_initial_upgrade_to_pro_shown"
const val WAS_APP_ICON_CUSTOMIZATION_WARNING_SHOWN = "was_app_icon_customization_warning_shown"
const val APP_SIDELOADING_STATUS = "app_sideloading_status"
const val DATE_FORMAT = "date_format"
const val WAS_OTG_HANDLED = "was_otg_handled_2"
const val WAS_UPGRADED_FROM_FREE_SHOWN = "was_upgraded_from_free_shown"
const val WAS_RATE_US_PROMPT_SHOWN = "was_rate_us_prompt_shown"
const val WAS_APP_RATED = "was_app_rated"
const val WAS_SORTING_BY_NUMERIC_VALUE_ADDED = "was_sorting_by_numeric_value_added"
const val WAS_FOLDER_LOCKING_NOTICE_SHOWN = "was_folder_locking_notice_shown"
const val LAST_RENAME_USED = "last_rename_used"
const val LAST_RENAME_PATTERN_USED = "last_rename_pattern_used"
const val LAST_EXPORTED_SETTINGS_FOLDER = "last_exported_settings_folder"
const val LAST_EXPORTED_SETTINGS_FILE = "last_exported_settings_file"
const val FONT_SIZE = "font_size"
const val WAS_MESSENGER_RECORDER_SHOWN = "was_messenger_recorder_shown"
const val DEFAULT_TAB = "default_tab"
const val START_NAME_WITH_SURNAME = "start_name_with_surname"
const val FAVORITES = "favorites"
const val ON_PROGRESS = 0
const val ON_COMPLETED = 1

// security
const val WAS_PROTECTION_HANDLED = "was_protection_handled"
const val PROTECTION_NONE = -1
const val PROTECTION_PATTERN = 0
const val PROTECTION_PIN = 1
const val PROTECTION_FINGERPRINT = 2

// sorting
const val SORT_ORDER = "sort_order"
const val SORT_FOLDER_PREFIX = "sort_folder_"       // storing folder specific values at using "Use for this folder only"
const val SORT_BY_NAME = 1
const val SORT_BY_DATE_MODIFIED = 2
const val SORT_BY_SIZE = 4
const val SORT_BY_DATE_TAKEN = 8
const val SORT_BY_EXTENSION = 16
const val SORT_BY_PATH = 32
const val SORT_BY_NUMBER = 64
const val SORT_BY_FIRST_NAME = 128
const val SORT_BY_MIDDLE_NAME = 256
const val SORT_BY_SURNAME = 512
const val SORT_DESCENDING = 1024
const val SORT_BY_TITLE = 2048
const val SORT_BY_ARTIST = 4096
const val SORT_BY_DURATION = 8192
const val SORT_BY_RANDOM = 16384
const val SORT_USE_NUMERIC_VALUE = 32768
const val SORT_BY_FULL_NAME = 65536
const val SORT_BY_CUSTOM = 131072

// conflict resolving
const val CONFLICT_SKIP = 1
const val CONFLICT_OVERWRITE = 2
const val CONFLICT_MERGE = 3
const val CONFLICT_KEEP_BOTH = 4

const val SIDELOADING_UNCHECKED = 0
const val SIDELOADING_TRUE = 1
const val SIDELOADING_FALSE = 2

const val DATE_FORMAT_ONE = "dd.MM.yyyy"
const val DATE_FORMAT_TWO = "dd/MM/yyyy"
const val DATE_FORMAT_THREE = "MM/dd/yyyy"
const val DATE_FORMAT_FOUR = "yyyy-MM-dd"
const val DATE_FORMAT_FIVE = "d MMMM yyyy"
const val DATE_FORMAT_SIX = "MMMM d yyyy"
const val DATE_FORMAT_SEVEN = "MM-dd-yyyy"
const val DATE_FORMAT_EIGHT = "dd-MM-yyyy"


// renaming
const val RENAME_SIMPLE = 0
const val RENAME_PATTERN = 1

// default tabs
const val TAB_LAST_USED = 0
const val TAB_CONTACTS = 1
const val TAB_FAVORITES = 2
const val TAB_CALL_HISTORY = 4
const val TAB_GROUPS = 8


fun isMarshmallowPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
fun isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
fun isNougatMR1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
fun isOreoMr1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
fun isPiePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
//fun isRPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R


const val TEL_PREFIX = "tel:"

const val FORBIDDEN_PHONE_CALL_NUMBER = "6505551212"

const val CALL_NEW_ITEM_MARKED = 1
const val CALL_ITEM_UN_MARKED = 0

const val USE_SIM_ID_PREFIX = "use_sim_id_"
const val SHOW_CHARACTER_COUNTER = "show_character_counter"

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()
fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}


var SCROLL_TO_POSITION:Int ? = null
var SCROLL_TO_POSITION_BY_TAPING: Int? = null
/**
 * funciton to scroll recyclerview to a position
 * @param position, the posoition to which the recyclerview should be scrolling
 */
fun scrollTOPosition(position:Int, layoutMngr: LinearLayoutManager ){
    val firstVisibleItemPosition = layoutMngr.findFirstVisibleItemPosition()
    val lastVisibleItemPosition = layoutMngr.findLastVisibleItemPosition()
    val middle = (firstVisibleItemPosition + lastVisibleItemPosition) / 2
//    layoutMngr.scrollToPositionWithOffset(position, middle)
    layoutMngr.scrollToPositionWithOffset(position, middle)
     SCROLL_TO_POSITION = null
}

/**
 * for storing positions of of search results
 * to be scrolled while clicking up and down arrow mark
 */
var scrollToPositions:MutableList<Int> = mutableListOf()
var scrollToCount : Int ? = null

fun getRandomColor(): Int {
    var random: Int
    val rand = Random()
    random = rand.nextInt(5 - 1) + 1
    return random
}