# BMS Widgets

تطبيق Android أصلي لويدجت سطح المكتب.

## V1.2
- إضافة ويدجت مدمجة للتاريخ + مواقيت الصلاة.
- القسم العلوي يعرض اليوم والتاريخ الميلادي والهجري والموقع والوقت.
- القسم السفلي يعرض الفجر، الشروق، الظهر، العصر، المغرب، العشاء.
- إعداد حجم الخط يغيّر الميلادي والهجري وبقية النصوص معًا.
- تمديد الويدجت أفقيًا أو عموديًا لا يغيّر حجم الخط تلقائيًا.
- الإبقاء على ويدجت التاريخ فقط وويدجت الصلاة فقط.
- الخلفيات: داكنة، شفافة، ليفر، النصر، السعودية.

## البناء
يتم بناء Debug APK تلقائيًا عبر GitHub Actions عند كل Push إلى main.


## V1.4
- Added explicit 12/24-hour clock selection.
- Increased separation between Hijri and Gregorian dates.
- Refined Liverpool red/white and Saudi green/white adaptive backgrounds.
- Removed BAS Platform widget from the exposed widget list.
- Widget backgrounds resize independently from text scaling.


## V1.8
- ربط ويدجت المهام مباشرة مع «مفكرتي» داخل Home Organizer.
- عرض مهام اليوم والمهام المتأخرة مع العدادات.
- المزامنة تتم تلقائيًا عند فتح أو تعديل «مفكرتي».
- الضغط على عنوان الويدجت أو أي مهمة يفتح Home Organizer.


## V2.1
- تصحيح مفهوم الربط: «مفكرتي» مصدر HTML مخزن داخل BMS Blank وليست Home Organizer مستقلًا.
- زر الربط يفتح BMS Blank بوضوح بدل الادعاء بفتح Home Organizer.
- تعليمات الويدجت تطلب فتح مفكرتي ثم الضغط على «تحديث ويدجت BAS».
