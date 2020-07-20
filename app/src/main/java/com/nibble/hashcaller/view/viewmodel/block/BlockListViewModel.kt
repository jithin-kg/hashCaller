import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.data.local.db.BlockedListPattern
import com.nibble.hashcaller.data.local.db.HashCallerDatabase
import com.nibble.hashcaller.data.repository.BlockListPatternRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 03,July,2020
 */
public class BlockListViewModel(application: Application) : AndroidViewModel(application) {

    private  val blockListPatternRepository: BlockListPatternRepository

    val allblockedList: LiveData<List<BlockedListPattern>>

    init {
        val blockedLIstDao = HashCallerDatabase.getDatabaseInstance(application).blocklistDAO()
        blockListPatternRepository = BlockListPatternRepository(blockedLIstDao)

        allblockedList = blockListPatternRepository.allBlockedList
    }

    //creating a coroutine to call suspending function
    //view models have their on scope we are launching coroutine on the viewmodelScope
    fun insert(blockedListPattern: BlockedListPattern) = viewModelScope.launch(Dispatchers.IO){
        blockListPatternRepository.insert(blockedListPattern)
    }
}