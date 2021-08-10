import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.local.db.blocklist.BlockedListPattern
import com.hashcaller.local.db.HashCallerDatabase
import com.hashcaller.repository.BlockListPatternRepository
import com.hashcaller.view.utils.CountrycodeHelper
import com.hashcaller.view.utils.LibPhoneCodeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 03,July,2020
 */
//todo ??

public class BlockListViewModel(application: Application) : AndroidViewModel(application) {

    private  val blockListPatternRepository: BlockListPatternRepository

    val allblockedList: LiveData<MutableList<BlockedListPattern>>?

    init {
        val blockedLIstDao = HashCallerDatabase.getDatabaseInstance(application).blocklistDAO()
        val mutedCallersDAO = HashCallerDatabase.getDatabaseInstance(application).mutedCallersDAO()
        val libCountryHelper: LibPhoneCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
        val countryCodeIso = CountrycodeHelper(application).getCountryISO()
        blockListPatternRepository = BlockListPatternRepository(
            blockedLIstDao,
            mutedCallersDAO,
            libCountryHelper,
            countryCodeIso
        )

        allblockedList = blockListPatternRepository.allBlockedList

    }

    //creating a coroutine to call suspending function
    //view models have their on scope we are launching coroutine on the viewmodelScope
    fun insert(blockedListPattern: BlockedListPattern) = viewModelScope.launch(Dispatchers.IO){
//        blockListPatternRepository.insert(blockedListPattern)
    }
}