import type { ReactNode, SVGProps } from "react";

type IconName = "alert" | "arrow" | "boxes" | "logout" | "package" | "refresh" | "shield" | "truck";

type IconProps = SVGProps<SVGSVGElement> & {
  name: IconName;
  size?: number;
};

const paths: Record<IconName, ReactNode> = {
  alert: <><circle cx="12" cy="12" r="10" /><path d="M12 8v4m0 4h.01" /></>,
  arrow: <><path d="M5 12h14m-7-7 7 7-7 7" /></>,
  boxes: <><path d="m12 3 9 5-9 5-9-5 9-5Z" /><path d="m3 12 9 5 9-5m-18 5 9 5 9-5M12 8v5" /></>,
  logout: <><path d="M10 17l5-5-5-5m5 5H3" /><path d="M12 3h6a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-6" /></>,
  package: <><path d="m12 3 9 5v8l-9 5-9-5V8l9-5Z" /><path d="m3 8 9 5 9-5m-9 5v8" /></>,
  refresh: <><path d="M20 7v5h-5M4 17v-5h5" /><path d="M5.5 9a7 7 0 0 1 12-2L20 12M4 12l2.5 5a7 7 0 0 0 12-2" /></>,
  shield: <><path d="M12 22s8-4 8-11V5l-8-3-8 3v6c0 7 8 11 8 11Z" /><path d="m9 12 2 2 4-4" /></>,
  truck: <><path d="M3 6h12v12H3z" /><path d="M15 10h4l3 3v5h-7" /><circle cx="7.5" cy="18" r="2" /><circle cx="17.5" cy="18" r="2" /></>,
};

export function Icon({ name, size = 18, ...props }: IconProps) {
  return (
    <svg
      aria-hidden="true"
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      {...props}
    >
      {paths[name]}
    </svg>
  );
}
