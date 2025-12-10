package androidx.lifecycle

import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

/**
 * Desktop stub of the Android SavedStateViewModelFactory.
 *
 * Navigation's back stack uses this factory by default. The desktop artifact's
 * implementation currently throws, so we fall back to new-instance creation to
 * keep zero-arg ViewModels working on desktop.
 */
@Suppress("unused")
class SavedStateViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        return ViewModelProvider.NewInstanceFactory().create(modelClass, extras)
    }
}
