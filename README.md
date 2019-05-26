# syncadapter
# Readme

Синхронизация контактов реализуется только при авторизации пользователя в AccountManager, поэтому сначала добавляем аккаунт нашего приложения https://developer.android.com/training/id-auth/custom_auth 

## Далее создаем SyncAdapter и SyncService

SyncAdapter : класс в котором мы реализуем метод onPerformSync, в этом методе пишем всю нашу синхронизацию 
(Запрос на сервер, затем проверка контактов в телефонной книге и добавление новых полей если есть совпадения), в сэмпле он пустой так как реальной синхронизации с сервером у нас нету . Далее пишем SyncService , Здесь важно в методе onCreate создать наш адаптер 
!!!!Адаптер создается в synchronized блоке!!!! 
```kotlin
override fun onCreate() {
    super.onCreate()
    Log.d(TAG, "Start sync service")
    synchronized(lock) {
        if (syncAdapter == null) {
            Log.d(TAG, "Start service")
            syncAdapter = SyncAdapter(applicationContext, true)
        }
    }
    Log.d(TAG, "Sync adapter created")
}
```
И в onBind возвращаем билдер нашего адаптера

```kotlin
override fun onBind(intent: Intent?): IBinder? = syncAdapter?.syncAdapterBinder
```
Теперь нам необходимо описать наш адаптер в xml файле. Для этого в package xml добавляем файл sync_adapter.xml 
```xml
<?xml version="1.0" encoding="utf-8"?>

<sync-adapter
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:contentAuthority="com.android.contacts"
        android:accountType="com.example.syncadapterapplication"
        android:supportsUploading="true"
        android:allowParallelSyncs="true"
        android:isAlwaysSyncable="true"
        android:userVisible="true" />
```
В параметре contentAuthority указываем имя провайдера с которым мы хотим синзронизироваться, а в accountType тип аккаунта нашего приложения. Для получения детальной информации об остальных параметрах sync-adapter’а переходи по ссылке [ссылке](https://developer.android.com/training/sync-adapters/creating-sync-adapter)

Если мы синхронизируемся с книгой контактов, то нам так же потребуется описать структуру добавляемых полей в xml, для этого создаем файл contacts.xml 
```xml
<?xml version="1.0" encoding="utf-8"?>
<ContactsAccountType xmlns:android="http://schemas.android.com/apk/res/android">
    <ContactsDataKind
            android:mimeType="vnd.android.cursor.item/com.example.syncadapterapplication"
            android:icon="@drawable/ic_launcher"
            android:summaryColumn="data2"
            android:detailColumn="data3"
            android:detailSocialSummary="true" />

</ContactsAccountType>
```
Подробную информацию по доступным полям книги контактов можно найти тут   [тут](https://developer.android.com/guide/topics/providers/contacts-provider?hl=ru)

Завершающим шагом , опишем наш SyncService в манифесте с указанием нашего адаптера синхронизации :
```xml
<service
        android:name=".syncservice.SyncService"
        android:exported="true">
    <intent-filter>
        <action android:name="android.content.SyncAdapter" />
    </intent-filter>
    <meta-data android:name="android.content.SyncAdapter" android:resource="@xml/sync_adapter" />
    <meta-data android:name="android.provider.CONTACTS_STRUCTURE" android:resource="@xml/contacts" />
</service>
```
И добавим Активити которую мы хотим открывать при нажатии на наше кастомное поле:
```xml
<activity android:name=".DefaultActivity" >
    <intent-filter android:icon="@drawable/ic_launcher_foreground" tools:ignore="AppLinkUrlError">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="vnd.android.cursor.item/com.example.syncadapterapplication" />
    </intent-filter>
</activity>
```
Теперь чтобы открыть нашу DefaultActivity из книги контактов , нам просто нужно добавить контакт с кастомным полем mimetype типа ‘vnd.android.cursor.item/com.example.syncadapterapplication’ туда же интентом нам придут все данные нашего контакта

## Важно добавить пермишены в манифест :
```xml
<manifest>
...
    <uses-permission
            android:name="android.permission.INTERNET"/>
    <uses-permission
            android:name="android.permission.READ_SYNC_SETTINGS"/>
    <uses-permission
            android:name="android.permission.WRITE_SYNC_SETTINGS"/>
    <uses-permission
            android:name="android.permission.AUTHENTICATE_ACCOUNTS"/>
...
</manifest>
```
В коде мы можем выставит автоматическую синхронизация с контактной книгой :
```kotlin
ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true)
```
Тогда при изменениях в контактной книге будет вызываться наш адаптер синхронизации, так же пользователь может сам включить или выключить опцию автоматической синхронизации в настройках

# ВАЖНО, для того чтобы добавить наше поле к существующему контакту, наш контакт должен совпадать примерно на 70% с существующим контактом, либо нам нужно мертвить контакты вручную по id(как достать id контакта по номеру телефона смотри ContactsManager в сэмпле):
```kotlin
op.add(ContentProviderOperation.newUpdate(ContactsContract.AggregationExceptions.CONTENT_URI)
    .withValue(ContactsContract.AggregationExceptions.TYPE,
        ContactsContract.AggregationExceptions.TYPE_KEEP_TOGETHER)
    .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID1, 0)
    .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID2, id)
    .build())
```



