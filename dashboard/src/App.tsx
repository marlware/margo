import { useEffect, useState } from 'react'
import './App.css'

interface Asset {
  id: string
  title: string
  durationSeconds: number | null
  hlsPath: string
  status: 'APPROVED' | 'REJECTED'
  errors: string[]
}

function App() {
  const [assets, setAssets] = useState<Asset[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const load = async () => {
      try {
        const res = await fetch('/assets')
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        setAssets(await res.json())
        setError(null)
      } catch (e) {
        setError(`Can't reach Margo (${(e as Error).message})`)
      }
    }
    load()
    const timer = setInterval(load, 3000)
    return () => clearInterval(timer)
  }, [])

  const rejected = assets.filter((a) => a.status === 'REJECTED').length

  return (
    <main>
      <h1>Margo</h1>
      <p>
        {assets.length} assets, {assets.length - rejected} approved, {rejected} rejected
      </p>
      {error && <p className="error">{error}</p>}
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Status</th>
            <th>Validation failures</th>
          </tr>
        </thead>
        <tbody>
          {assets.map((a) => (
            <tr key={a.id}>
              <td>{a.id}</td>
              <td>{a.title}</td>
              <td className={a.status === 'APPROVED' ? 'ok' : 'bad'}>{a.status}</td>
              <td>{a.errors.join(', ')}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </main>
  )
}

export default App
