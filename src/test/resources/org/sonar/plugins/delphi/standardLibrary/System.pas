{*******************************************************}
{                                                       }
{           CodeGear Delphi Runtime Library             }
{                                                       }
{ Copyright(c) 1995-2012 Embarcadero Technologies, Inc. }
{                                                       }
{   Copyright and license exceptions noted in source    }
{                                                       }
{*******************************************************}

unit System;

interface

type
  TObject = class;

  TClass = class of TObject;

  PMethod = ^TMethod;
  TMethod = record
    Code, Data: Pointer;
  public
    class operator Equal(const Left, Right: TMethod): Boolean;
    class operator NotEqual(const Left, Right: TMethod): Boolean;
    class operator GreaterThan(const Left, Right: TMethod): Boolean;
    class operator GreaterThanOrEqual(const Left, Right: TMethod): Boolean;
    class operator LessThan(const Left, Right: TMethod): Boolean;
    class operator LessThanOrEqual(const Left, Right: TMethod): Boolean;
  end;

  TObject = class
  public
    constructor Create;
    procedure Free;
    class function InitInstance(Instance: Pointer): TObject;
    procedure CleanupInstance;
    function ClassType: TClass; inline;
    class function ClassName: string;
    class function ClassNameIs(const Name: string): Boolean;
    class function ClassParent: TClass;
    class function ClassInfo: Pointer; inline;
    class function InstanceSize: Longint; inline;
    class function InheritsFrom(AClass: TClass): Boolean;
    class function MethodAddress(const Name: _ShortStr): Pointer; overload;
    class function MethodAddress(const Name: string): Pointer; overload;
    class function MethodName(Address: Pointer): string;
    class function QualifiedClassName: string;
    function FieldAddress(const Name: _ShortStr): Pointer; overload;
    function FieldAddress(const Name: string): Pointer; overload;
    function GetInterface(const IID: TGUID; out Obj): Boolean;
    class function GetInterfaceEntry(const IID: TGUID): PInterfaceEntry;
    class function GetInterfaceTable: PInterfaceTable;
    class function UnitName: string;
    class function UnitScope: string;
    function Equals(Obj: TObject): Boolean; virtual;
    function GetHashCode: Integer; virtual;
    function ToString: string; virtual;
    function SafeCallException(ExceptObject: TObject;
      ExceptAddr: Pointer): HResult; virtual;
    procedure AfterConstruction; virtual;
    procedure BeforeDestruction; virtual;
    procedure Dispatch(var Message); virtual;
    procedure DefaultHandler(var Message); virtual;
    class function NewInstance: TObject {$IFDEF AUTOREFCOUNT} unsafe {$ENDIF}; virtual;
    procedure FreeInstance; virtual;
    destructor Destroy; virtual;
  end;

  IInterface = interface
    ['{00000000-0000-0000-C000-000000000046}']
    function QueryInterface(const IID: TGUID; out Obj): HResult; stdcall;
    function _AddRef: Integer; stdcall;
    function _Release: Integer; stdcall;
  end;

  IEnumerator = interface(IInterface)
    function GetCurrent: TObject;
    function MoveNext: Boolean;
    procedure Reset;
    property Current: TObject read GetCurrent;
  end;

  IEnumerable = interface(IInterface)
    function GetEnumerator: IEnumerator;
  end;

  IEnumerator<T> = interface(IEnumerator)
    function GetCurrent: T;
    property Current: T read GetCurrent;
  end;

  IEnumerable<T> = interface(IEnumerable)
    function GetEnumerator: IEnumerator<T>;
  end;

  IComparable = interface(IInterface)
    function CompareTo(Obj: TObject): Integer;
  end;

  IComparable<T> = interface(IComparable)
    function CompareTo(Value: T): Integer;
  end;

  IEquatable<T> = interface(IInterface)
    function Equals(Value: T): Boolean;
  end;

implementation

end.
