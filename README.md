# GestureLockView
###手势解锁
[学习大神的连接](http://blog.csdn.net/lmj623565791/article/details/36236113)
####想要实现的效果：<br>
  * 可以设置初始模式：设置初始密码模式。描述(设置方法进入设置模式，需要回调接口，每个状态执行一个回调函数，设置成功后将数据返回)
  * 可以选择显示的模式：如圆环型(参考拍卖)，全部填充，自定义图片。
  * 可以自定义连接线的：颜色，宽度，有无。
  * 可以设置箭头的颜色和有无。
  * 可以设置内部圆点的大小。
  * 可以设置最少连接圆点的个数。
  * 自定义的属性：`GestureLockView`：箭头的大小(mArrowRate)->相对于外圆半径的百分比，内圆半径(mInnerCircleRadiusRate)->相对于外圆半径的百     分比。`GestureLockViewGroup`：路径的宽度->(`mPaint.setStrokeWidth(mGestureLockViewWidth * 0.29f`);)，路径是否透明->(mPaint.setAlpha(50))
  * 触摸状态在原有的状态中将手指离开的状态(`Mode.STATUS_FINGER_UP`)拆分成两种状态(`Mode.STATUS_FINGER_UP_MATCH`和`Mode.STATUS_FINGER_UP_UNMATCH`)
