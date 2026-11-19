# SmoothBG (Fabric, Minecraft 26.3)

มอดฝั่ง client ที่ไม่ใช้ Mixin เลย (เสี่ยงพังน้อยที่สุดเมื่อ Minecraft อัปเดต)
และไม่แตะภาพ, particle, shader หรือการเรนเดอร์ตอนเล่นปกติ

## ทำอะไร
1. เธรดพื้นหลังลำดับต่ำสุด ใช้ CPU ไม่เกิน dutyCycle (ค่าเริ่มต้น 10%) และหยุดเองเมื่อ tick เฉลี่ยช้ากว่า busyTickMs
2. ตอน alt-tab: จำกัด FPS ชั่วคราวเหลือ unfocusedFps แล้วคืนค่าเดิมทันทีที่กลับมา
3. ไม่โฟกัสนาน idleGcSeconds: เก็บกวาดหน่วยความจำ 1 ครั้ง (ลด RAM ค้างตอนทิ้งเกมไว้)

## Build
1. ติดตั้ง JDK 25 และ Gradle 9.6.0
2. ในโฟลเดอร์นี้รัน: `gradle wrapper --gradle-version 9.6.0` แล้ว `./gradlew build`
3. ไฟล์ได้ที่ build/libs/smoothbg-1.0.0.jar (ไม่เอา -sources) ใส่โฟลเดอร์ mods
4. เปลี่ยน fabric_version ใน gradle.properties เป็นตัวล่าสุดสำหรับ 26.3 ก่อน build

## JVM arguments (ได้ผลมากกว่ามอดใดๆ) — ใส่ใน Launcher > Installations > More options
แก้ 4G เป็นประมาณครึ่งหนึ่งของ RAM เครื่อง (ไม่เกิน 8G):

-Xms4G -Xmx4G -XX:+UseG1GC -XX:MaxGCPauseMillis=40 -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=16M -XX:+ParallelRefProcEnabled -XX:+DisableExplicitGC -XX:+AlwaysPreTouch

(ถ้าใช้ idleGcSeconds ให้เอา -XX:+DisableExplicitGC ออก)

## ชุดมอดที่แนะนำควบคู่ (Modrinth, Fabric 26.3)
Sodium, Lithium, FerriteCore, ModernFix, ImmediatelyFast, ScalableLux, Entity Culling
