import { Icon } from '../ui/Icon'
import './Brand.css'

export function Brand({ light = false }: { light?: boolean }) {
  return (
    <div className={`brand ${light ? 'brand-light' : ''}`}>
      <span className="brand-mark">
        <Icon name="bank" size={26} />
      </span>
      <span>
        Bank of CLI<span className="brand-caption">PERSONAL BANKING</span>
      </span>
    </div>
  )
}
