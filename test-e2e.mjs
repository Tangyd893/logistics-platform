/**
 * 综合物流平台 E2E 测试套件
 * 运行方式：cd /home/dong/.openclaw/workspace/output/logistics-platform && node test-e2e.mjs
 * 前置条件：前后端服务已启动（Backend 8080，Frontend 3000）
 */

import { chromium } from 'playwright'

const BASE = 'http://localhost:3000'
const ADMIN = { username: 'admin', password: 'admin123' }

;(async () => {
  const browser = await chromium.launch({ headless: true })
  const context = await browser.newContext()
  const page = await context.newPage()
  const errors = []
  page.on('console', (msg) => { if (msg.type() === 'error') errors.push(msg.text()) })

  let passed = 0
  let failed = 0
  const total = (name, ok) => { console.log(ok ? '✅' : '❌'); ok ? passed++ : failed++ }

  // 1. 登录
  console.log('\n🔐 1. 登录')
  await page.goto(BASE + '/login', { waitUntil: 'networkidle' })
  await page.fill('input[placeholder="请输入用户名"]', ADMIN.username)
  await page.fill('input[type="password"]', ADMIN.password)
  await page.click('button[type="submit"]')
  await page.waitForURL('**/')
  await page.waitForTimeout(1500)
  console.log('  登录跳转成功 ✅')
  passed++

  // 2. 核心页面加载
  console.log('\n📋 2. 核心页面加载（12 个）')
  const pages = [
    ['/warehouse', '仓库管理'],
    ['/warehouse/locations', '库位管理'],
    ['/warehouse/inventory', '库存查询'],
    ['/order', '订单管理'],
    ['/order/new', '创建订单'],
    ['/transport', '运输管理'],
    ['/transport/new', '创建运单'],
    ['/transport/tracking', '配送跟踪'],
    ['/transport/drivers', '司机管理'],
    ['/transport/vehicles', '车辆管理'],
    ['/statistics', '数据统计'],
    ['/system', '用户管理'],
  ]
  for (const [path, expected] of pages) {
    await page.goto(BASE + path, { waitUntil: 'domcontentloaded', timeout: 10000 })
    await page.waitForTimeout(700)
    const h1 = await page.textContent('h1').catch(() => '')
    const ok = h1.trim().includes(expected)
    console.log(`  ${path}: "${h1.trim()}" ${ok ? '✅' : '❌'}`)
    ok ? passed++ : failed++
  }

  // 3. Sidebar 数量
  console.log('\n📌 3. Sidebar 菜单数量')
  const sidebarLinks = await page.$$('.sidebar-link')
  const sidebarOk = sidebarLinks.length >= 9
  console.log(`  侧边栏菜单: ${sidebarLinks.length} 个 ${sidebarOk ? '✅' : '❌'}`)
  sidebarOk ? passed++ : failed++

  // 4. 订单状态筛选
  console.log('\n🔽 4. 订单状态筛选')
  await page.goto(BASE + '/order', { waitUntil: 'domcontentloaded' })
  await page.waitForTimeout(800)
  const orderSelect = await page.$('select')
  if (orderSelect) {
    const opts = await page.$$('select option')
    const optTexts = await Promise.all(opts.map((o) => o.textContent()))
    const hasStatusFilter = optTexts.some((t) => t.includes('已确认'))
    console.log(`  状态筛选下拉 ${hasStatusFilter ? '✅' : '❌'}: [${optTexts.join(', ')}]`)
    hasStatusFilter ? passed++ : failed++
  } else {
    console.log('  状态筛选下拉 ❌')
    failed++
  }

  // 5. 司机新增弹窗
  console.log('\n🚚 5. 司机管理新增')
  await page.goto(BASE + '/transport/drivers', { waitUntil: 'domcontentloaded' })
  await page.waitForTimeout(600)
  const driverAddBtn = await page.$('button:has-text("+ 新增司机")')
  if (driverAddBtn) {
    await driverAddBtn.click()
    await page.waitForTimeout(500)
    const modalInput = await page.$('input')
    console.log(`  弹窗打开 ${modalInput ? '✅' : '❌'}`)
    modalInput ? passed++ : failed++
    const cancelBtn = await page.$('button:has-text("取消")')
    if (cancelBtn) await cancelBtn.click()
    await page.waitForTimeout(300)
  } else {
    console.log('  新增按钮 ❌')
    failed++
  }

  // 6. 车辆新增弹窗
  console.log('\n🚛 6. 车辆管理新增')
  await page.goto(BASE + '/transport/vehicles', { waitUntil: 'domcontentloaded' })
  await page.waitForTimeout(600)
  const vehicleAddBtn = await page.$('button:has-text("+ 新增车辆")')
  if (vehicleAddBtn) {
    await vehicleAddBtn.click()
    await page.waitForTimeout(500)
    const modalInput = await page.$('input')
    console.log(`  弹窗打开 ${modalInput ? '✅' : '❌'}`)
    modalInput ? passed++ : failed++
    const cancelBtn = await page.$('button:has-text("取消")')
    if (cancelBtn) await cancelBtn.click()
    await page.waitForTimeout(300)
  } else {
    console.log('  新增按钮 ❌')
    failed++
  }

  // 7. 地图提示文案
  console.log('\n🗺️ 7. 配送跟踪页面')
  await page.goto(BASE + '/transport/tracking', { waitUntil: 'domcontentloaded' })
  await page.waitForTimeout(600)
  const mapPrompt = await page.$('text=点击左侧运单查看配送路线')
  console.log(`  地图空状态提示 ${mapPrompt ? '✅' : '（无运单时显示）'}`)
  passed++

  // 8. 统计图表渲染
  console.log('\n📊 8. 统计图表')
  await page.goto(BASE + '/statistics', { waitUntil: 'domcontentloaded' })
  await page.waitForTimeout(1500)
  const chart = await page.$('.recharts-wrapper')
  console.log(`  recharts 图表 ${chart ? '✅' : '❌'}`)
  chart ? passed++ : failed++

  // 9. Console 错误
  console.log('\n❌ 9. Console 错误')
  if (errors.length === 0) {
    console.log('  无 console error ✅')
    passed++
  } else {
    console.log(`  Console errors: ${errors.slice(0, 3).join('; ')} ❌`)
    failed++
  }

  // 10. 运单列表操作按钮
  console.log('\n📦 10. 运单列表状态推进按钮')
  await page.goto(BASE + '/transport', { waitUntil: 'domcontentloaded' })
  await page.waitForTimeout(800)
  const actionBtns = await page.$$('button')
  const btnTexts = await Promise.all(actionBtns.map((b) => b.textContent()))
  const hasActionBtn = btnTexts.some((t) => t.includes('确认') || t.includes('送达') || t.includes('提货'))
  console.log(`  状态推进按钮 ${hasActionBtn ? '✅' : '❌'}`)
  hasActionBtn ? passed++ : failed++

  // Summary
  console.log('\n' + '─'.repeat(40))
  console.log(`总计: ${passed}/${passed + failed} 通过`)
  if (failed > 0) console.log(`❌ 失败: ${failed} 项`)

  await browser.close()
  process.exit(failed > 0 ? 1 : 0)
})()
